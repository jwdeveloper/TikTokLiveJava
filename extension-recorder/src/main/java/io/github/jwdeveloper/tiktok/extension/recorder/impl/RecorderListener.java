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
import io.github.jwdeveloper.tiktok.data.events.TikTokLiveEndedEvent;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.extension.recorder.api.LiveRecorder;
import io.github.jwdeveloper.tiktok.data.events.TikTokConnectedEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokDisconnectedEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.extension.recorder.impl.data.DownloadData;
import io.github.jwdeveloper.tiktok.extension.recorder.impl.data.RecorderSettings;
import io.github.jwdeveloper.tiktok.data.events.http.TikTokRoomDataResponseEvent;
import io.github.jwdeveloper.tiktok.extension.recorder.impl.enums.LiveQuality;
import io.github.jwdeveloper.tiktok.extension.recorder.impl.event.TikTokLiveRecorderStartedEvent;
import io.github.jwdeveloper.tiktok.http.HttpClientFactory;
import io.github.jwdeveloper.tiktok.live.LiveClient;

import javax.net.ssl.HttpsURLConnection;
import java.io.*;
import java.net.URL;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RecorderListener implements LiveRecorder {

    private final Consumer<RecorderSettings> consumer;
    private RecorderSettings settings;
    private DownloadData downloadData;
    private Thread liveDownloadThread;

    public RecorderListener(Consumer<RecorderSettings> consumer) {
        this.consumer = consumer;
    }

    @Override
    public void pause() {

    }

    @Override
    public void unpause() {

    }

    @TikTokEventObserver
    private void onResponse(LiveClient liveClient, TikTokRoomDataResponseEvent event) {
        settings = RecorderSettings.DEFAULT();
        consumer.accept(settings);

        var json = event.getLiveData().getJson();

        liveClient.getLogger().info("Searching for live download url");
        if (settings.getPrepareDownloadData() != null) {
            downloadData = settings.getPrepareDownloadData().apply(json);
        } else {
            downloadData = mapToDownloadData(json);
        }

        if (downloadData.getDownloadLiveUrl().isEmpty()) {
            throw new TikTokLiveException("Unable to find download live url!");
        }
        liveClient.getLogger().info("Live download url found!");

    }

    @TikTokEventObserver
    private void onConnected(LiveClient liveClient, TikTokConnectedEvent event) {
        liveDownloadThread = new Thread(() ->
        {
            try {
                var bufferSize = 1024;
                var url = new URL(downloadData.getFullUrl());
                HttpsURLConnection socksConnection = (HttpsURLConnection) url.openConnection();
                var headers = LiveClientSettings.DefaultRequestHeaders();
                for (var entry : headers.entrySet()) {
                    socksConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }

                try (var in = new BufferedInputStream(socksConnection.getInputStream())) {
                    var path = settings.getOutputPath() + File.separator + settings.getOutputFileName();
                    var file = new File(path);
                    file.getParentFile().mkdirs();
                    var fileOutputStream = new FileOutputStream(file);
                    byte dataBuffer[] = new byte[bufferSize];
                    int bytesRead;
                    while ((bytesRead = in.read(dataBuffer, 0, bufferSize)) != -1) {
                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                    }
                } catch (IOException e) {
                    throw e;
                }
            } catch (Exception e) {
                e.printStackTrace();
                ;
            }

        });


        liveDownloadThread.start();
    }


    private static void downloadUsingStream(String urlStr, String file) throws IOException {
        URL url = new URL(urlStr);
        BufferedInputStream bis = new BufferedInputStream(url.openStream());
        FileOutputStream fis = new FileOutputStream(file);
        byte[] buffer = new byte[1024];
        int count = 0;
        while ((count = bis.read(buffer, 0, 1024)) != -1) {
            fis.write(buffer, 0, count);
        }
        fis.close();
        bis.close();
    }


    @TikTokEventObserver
    private void onDisconnected(LiveClient liveClient, TikTokDisconnectedEvent event) {
        liveDownloadThread.interrupt();
    }

    @TikTokEventObserver
    private void onDisconnected(LiveClient liveClient, TikTokLiveEndedEvent event) {
        liveDownloadThread.interrupt();
    }

    private int terminateFfmpeg(final Process process) {
        if (!process.isAlive()) {
            /*
             * ffmpeg -version, do nothing
             */
            return process.exitValue();
        }

        /*
         * ffmpeg -f x11grab
         */
        System.out.println("About to destroy the child process...");
        try (final OutputStreamWriter out = new OutputStreamWriter(process.getOutputStream(), UTF_8)) {
            out.write('q');
        } catch (final IOException ioe) {
            ioe.printStackTrace();
        }
        try {
            if (!process.waitFor(5L, TimeUnit.SECONDS)) {
                process.destroy();
                process.waitFor();
            }
            return process.exitValue();
        } catch (final InterruptedException ie) {
            System.out.println("Interrupted");
            ie.printStackTrace();
            Thread.currentThread().interrupt();
            return -1;
        }
    }

    private DownloadData mapToDownloadData(String json) {

        var parsedJson = JsonParser.parseString(json);
        var jsonObject = parsedJson.getAsJsonObject();
        var streamDataJson = jsonObject.getAsJsonObject("data")
                .getAsJsonObject("stream_url")
                .getAsJsonObject("live_core_sdk_data")
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
    }


}
