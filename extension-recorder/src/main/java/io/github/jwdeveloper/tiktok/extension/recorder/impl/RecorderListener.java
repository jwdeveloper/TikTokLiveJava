/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.extension.recorder.impl;

import com.google.gson.JsonParser;
import io.github.jwdeveloper.tiktok.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.control.TikTokPreConnectionEvent;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.extension.recorder.api.LiveRecorder;
import io.github.jwdeveloper.tiktok.extension.recorder.impl.data.*;
import io.github.jwdeveloper.tiktok.extension.recorder.impl.enums.LiveQuality;
import io.github.jwdeveloper.tiktok.extension.recorder.impl.event.TikTokLiveRecorderStartedEvent;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.models.ConnectionState;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.function.BiConsumer;

public class RecorderListener implements LiveRecorder {

    private final BiConsumer<RecorderSettings, LiveClient> consumer;
    private final RecorderSettings settings;
    private DownloadData downloadData;
    private Thread liveDownloadThread;

    public RecorderListener(BiConsumer<RecorderSettings, LiveClient> consumer) {
        this.consumer = consumer;
        this.settings = RecorderSettings.DEFAULT();
    }

    @TikTokEventObserver
    private void onResponse(LiveClient liveClient, TikTokPreConnectionEvent event) {
        consumer.accept(settings, liveClient);

        var json = event.getUserData().getJson();

        liveClient.getLogger().info("Searching for live download url");
        downloadData = settings.getPrepareDownloadData() != null ?
                settings.getPrepareDownloadData().apply(json) :
                mapToDownloadData(json);

        if (downloadData.getDownloadLiveUrl().isEmpty())
            liveClient.getLogger().warning("Unable to find download live url!");
        else
            liveClient.getLogger().info("Live download url found!");
    }

    @TikTokEventObserver
    private void onConnected(LiveClient liveClient, TikTokConnectedEvent event) {
        if (isConnected() || downloadData.getDownloadLiveUrl().isEmpty())
            return;

        liveDownloadThread = new Thread(() -> {
            try {
                liveClient.getLogger().info("Recording started "+liveClient.getRoomInfo().getHostName());

				HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(URI.create(downloadData.getFullUrl())).GET();
                for (var entry : LiveClientSettings.DefaultRequestHeaders().entrySet())
					requestBuilder.header(entry.getKey(), entry.getValue());
                HttpResponse<InputStream> serverResponse = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.NORMAL)
                    .connectTimeout(Duration.ofSeconds(10)).build().send(requestBuilder.build(), HttpResponse.BodyHandlers.ofInputStream());

                var file = settings.getOutputFile();
                file.getParentFile().mkdirs();
                file.createNewFile();

                try (
                    var in = serverResponse.body();
                    var fos = new FileOutputStream(file, true)
                ) {
                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;
                    while ((!settings.isStopOnDisconnect() || liveClient.getRoomInfo().getConnectionState() == ConnectionState.CONNECTED) && (bytesRead = in.read(dataBuffer)) != -1) {
                        fos.write(dataBuffer, 0, bytesRead);
                        fos.flush();
                    }
                } catch (IOException ignored) {
                } finally {
                    liveClient.getLogger().severe("Stopped recording " + liveClient.getRoomInfo().getHostName());
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        var recordingStartedEvent = new TikTokLiveRecorderStartedEvent(downloadData);
        liveClient.publishEvent(recordingStartedEvent);
        if (recordingStartedEvent.isCanceled())
			liveClient.getLogger().info("Recording cancelled");
		else
            liveDownloadThread.start();
    }

    @TikTokEventObserver
    private void onDisconnected(LiveClient liveClient, TikTokDisconnectedEvent event) {
        if (isConnected() && settings.isStopOnDisconnect())
            liveDownloadThread.interrupt();
    }

    @TikTokEventObserver
    private void onLiveEnded(LiveClient liveClient, TikTokLiveEndedEvent event) {
        if (isConnected())
            liveDownloadThread.interrupt();
    }


    private DownloadData mapToDownloadData(String json) {
        try {
            var parsedJson = JsonParser.parseString(json);
            var jsonObject = parsedJson.getAsJsonObject();
            var streamDataJson = jsonObject.getAsJsonObject("data")
                .getAsJsonObject("liveRoom")
                .getAsJsonObject("streamData")
                .getAsJsonObject("pull_data")
                .get("stream_data")
                .getAsString();

            var streamDataJsonObject = JsonParser.parseString(streamDataJson).getAsJsonObject();

            var urlLink = streamDataJsonObject.getAsJsonObject("data")
                .getAsJsonObject(LiveQuality.origin.name())
                .getAsJsonObject("main")
                .get("flv")
                .getAsString();

            var sessionId = streamDataJsonObject.getAsJsonObject("common")
                .get("session_id")
                .getAsString();

            //main
            //https://pull-f5-tt03.fcdn.eu.tiktokcdn.com/stage/stream-3284937501738533765.flv?session_id=136-20240109000954BF818F1B3A8E5E39E238&_webnoredir=1
            //Working
            //https://pull-f5-tt03.fcdn.eu.tiktokcdn.com/game/stream-3284937501738533765_sd5.flv?_session_id=136-20240109001052D91FDBC00143211020C8.1704759052997&_webnoredir=1
            //https://pull-f5-tt02.fcdn.eu.tiktokcdn.com/stage/stream-3861399216374940610_uhd5.flv?_session_id=136-20240109000223D0BAA1A83974490EE630.1704758544391&_webnoredir=1

            return new DownloadData(urlLink, sessionId);
        } catch (Exception e) {
            return new DownloadData("", "");
        }
    }

    private boolean isConnected() {
        return liveDownloadThread != null && liveDownloadThread.isAlive();
    }
}