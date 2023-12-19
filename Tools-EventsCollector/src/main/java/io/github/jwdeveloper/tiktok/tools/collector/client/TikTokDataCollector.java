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
package io.github.jwdeveloper.tiktok.tools.collector.client;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.http.TikTokHttpResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketMessageEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketResponseEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageException;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.tools.collector.api.DataCollector;
import io.github.jwdeveloper.tiktok.tools.collector.api.TikTokDataCollectorModel;
import io.github.jwdeveloper.tiktok.tools.db.TikTokDatabase;
import io.github.jwdeveloper.tiktok.tools.db.tables.ExceptionInfoModel;
import io.github.jwdeveloper.tiktok.tools.db.tables.TikTokDataTable;
import io.github.jwdeveloper.tiktok.tools.db.tables.TikTokErrorModel;
import io.github.jwdeveloper.tiktok.utils.JsonUtil;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class TikTokDataCollector implements DataCollector {
    private final TikTokDataCollectorModel dataCollectorModel;
    private final TikTokDatabase tikTokDatabase;
    private final List<LiveClient> tiktokClients;

    public TikTokDataCollector(TikTokDataCollectorModel dataCollectorModel, TikTokDatabase tikTokDatabase) {
        this.dataCollectorModel = dataCollectorModel;
        this.tikTokDatabase = tikTokDatabase;
        this.tiktokClients = new ArrayList<>();
    }

    public void connect() {
        try {
            if (!tikTokDatabase.isConnected()) {
                tikTokDatabase.connect();
            }
            for (var user : dataCollectorModel.getUsers()) {
                var client = createLiveClient(user);
                tiktokClients.add(client);
                client.connectAsync();
            }
        } catch (Exception e) {
            throw new RuntimeException("Unable to start tiktok connector", e);
        }
    }


    public void disconnect() {
        disconnect(false);
    }

    @Override
    public void disconnect(boolean keepDatabase) {
        try {
            for (var client : tiktokClients) {
                client.disconnect();
            }
            if (!keepDatabase) {
                tikTokDatabase.close();
            }

        } catch (Exception e) {
            throw new RuntimeException("Unable to stop tiktok connector", e);
        }
    }

    public LiveClient createLiveClient(String tiktokUser) {
        var builder = TikTokLive.newClient(tiktokUser);
        builder.onConnected((liveClient, event) ->
                {
                    liveClient.getLogger().info("Connected to " + liveClient.getRoomInfo().getHostName());
                })
                .onDisconnected((liveClient, event) ->
                {
                    liveClient.getLogger().info("Disconnected " + liveClient.getRoomInfo().getHostName());
                })
                .onWebsocketResponse(this::handleResponseAndMessages)
                .onWebsocketMessage(this::handleMappedEvent)
                .onHttpResponse((liveClient, event) ->
                {
                    var data = createHttpResponseData(event, tiktokUser);
                    tikTokDatabase.insertData(data);
                })
                .onError(this::handleError);
        dataCollectorModel.getOnConfigureLiveClient().accept(builder);
        return builder.build();
    }

    private void handleResponseAndMessages(LiveClient client, TikTokWebsocketResponseEvent event) {
        var responseData = createResponseData(event.getResponse(), client.getRoomInfo().getHostName());
        tikTokDatabase.insertData(responseData);

        var filter = dataCollectorModel.getMessagesFilter();
        for (var message : event.getResponse().getMessagesList()) {
            if (filter.isEmpty()) {
                var data = createMessageData(message, client.getRoomInfo().getHostName());
                tikTokDatabase.insertData(data);
                continue;
            }
            if (!filter.contains(message.getMethod())) {
                continue;
            }
            var data = createMessageData(message, client.getRoomInfo().getHostName());
            tikTokDatabase.insertData(data);
        }
    }

    private void handleMappedEvent(LiveClient client, TikTokWebsocketMessageEvent messageEvent) {
        var event = messageEvent.getEvent();
        var eventName = event.getClass().getSimpleName();

        var filter = dataCollectorModel.getEventsFilter();

        if (filter.isEmpty()) {
            var data = createEventData(event, client.getRoomInfo().getHostName());
            tikTokDatabase.insertData(data);
            return;
        }

        if (!filter.contains(eventName)) {
            return;
        }
        var data = createEventData(event, client.getRoomInfo().getHostName());
        tikTokDatabase.insertData(data);
    }

    private void handleError(LiveClient client, TikTokErrorEvent event) {
        var exception = event.getException();
        var userName = client.getRoomInfo().getHostName();
        var exceptionContent = ExceptionInfoModel.getStackTraceAsString(exception);
        var errorModel = new TikTokErrorModel();
        if (exception instanceof TikTokLiveMessageException ex) {
            errorModel.setHostName(userName);
            errorModel.setErrorName(ex.messageMethod());
            errorModel.setErrorType("error-message");
            errorModel.setExceptionContent(exceptionContent);
            errorModel.setMessage(ex.messageToBase64());
            errorModel.setResponse(ex.webcastResponseToBase64());
        } else {
            errorModel.setHostName(userName);
            errorModel.setErrorName(exception.getClass().getSimpleName());
            errorModel.setErrorType("error-system");
            errorModel.setExceptionContent(exceptionContent);
            errorModel.setMessage("");
            errorModel.setResponse("");
        }


        tikTokDatabase.insertError(errorModel);
        client.getLogger().info("ERROR: " + errorModel.getErrorName());
        exception.printStackTrace();
    }

    private TikTokDataTable createHttpResponseData(TikTokHttpResponseEvent response, String tiktokUser) {
        var base64 = JsonUtil.toJson(response);
        var data = new TikTokDataTable();
        data.setSessionTag(dataCollectorModel.getSessionTag());
        data.setTiktokUser(tiktokUser);
        data.setDataType("response");
        data.setDataTypeName("Http");
        data.setContent(base64);
        return data;
    }


    private TikTokDataTable createResponseData(WebcastResponse response, String tiktokUser) {
        var base64 = Base64.getEncoder().encodeToString(response.toByteArray());
        var data = new TikTokDataTable();
        data.setSessionTag(dataCollectorModel.getSessionTag());
        data.setTiktokUser(tiktokUser);
        data.setDataType("response");
        data.setDataTypeName("WebcastResponse");
        data.setContent(base64);
        return data;
    }

    private TikTokDataTable createMessageData(WebcastResponse.Message message, String tiktokUser) {
        var base64 = Base64.getEncoder().encodeToString(message.getPayload().toByteArray());
        var data = new TikTokDataTable();
        data.setSessionTag(dataCollectorModel.getSessionTag());
        data.setTiktokUser(tiktokUser);
        data.setDataType("message");
        data.setDataTypeName(message.getMethod());
        data.setContent(base64);
        return data;
    }

    private TikTokDataTable createEventData(TikTokEvent event, String tiktokUser) {
        var base64 = JsonUtil.toJson(event);
        var data = new TikTokDataTable();
        data.setSessionTag(dataCollectorModel.getSessionTag());
        data.setTiktokUser(tiktokUser);
        data.setDataType("event");
        data.setDataTypeName(event.getClass().getSimpleName());
        data.setContent(base64);
        return data;
    }
}
