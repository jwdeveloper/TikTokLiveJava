package io.github.jwdeveloper.tiktok.extension.collector.impl;

import com.google.gson.*;
import io.github.jwdeveloper.tiktok.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.control.TikTokConnectingEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketResponseEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageException;
import io.github.jwdeveloper.tiktok.extension.collector.api.LiveDataCollector;
import io.github.jwdeveloper.tiktok.extension.collector.api.data.CollectorListenerSettings;
import io.github.jwdeveloper.tiktok.extension.collector.api.file.FileDataCollectorSettings;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.utils.JsonUtil;

import java.io.*;
import java.nio.file.*;
import java.util.*;

public class FileDataCollectorListener implements LiveDataCollector {

    private final FileDataCollectorSettings fileSettings;
    private final CollectorListenerSettings collectorSettings;
    private String sessionId;
    private String userName;

    public FileDataCollectorListener(FileDataCollectorSettings fileSettings, CollectorListenerSettings collectorSettings) {
        this.fileSettings = fileSettings;
        this.collectorSettings = collectorSettings;
    }

    @TikTokEventObserver
    private void onResponse(LiveClient liveClient, TikTokWebsocketResponseEvent event) {
        includeResponse(liveClient, event.getResponse());
        event.getResponse().getMessagesList().forEach(message -> includeMessage(liveClient, message));
    }

    @TikTokEventObserver
    private void onEvent(LiveClient liveClient, TikTokEvent event) {
        if (event instanceof TikTokConnectingEvent) {
            sessionId = UUID.randomUUID().toString();
            userName = liveClient.getRoomInfo().getHostName();
        }

        if (event instanceof TikTokErrorEvent) {
            return;
        }

        includeEvent(event);
    }

    @TikTokEventObserver
    private void onError(LiveClient liveClient, TikTokErrorEvent event) {
        event.getException().printStackTrace();
        includeError(event);
    }


    private void includeResponse(LiveClient liveClient, WebcastResponse message) {
        var messageContent = Base64.getEncoder().encodeToString(message.toByteArray());
        saveJson(createJson("response", "webcast", messageContent));
    }

    private void includeMessage(LiveClient liveClient, WebcastResponse.Message message) {
        var method = message.getMethod();
        var messageContent = Base64.getEncoder().encodeToString(message.getPayload().toByteArray());

        saveJson(createJson("message", method, messageContent));
    }

    private void includeEvent(TikTokEvent event) {
        var json = JsonUtil.toJson(event);
        var content = Base64.getEncoder().encodeToString(json.getBytes());
        var name = event.getClass().getSimpleName();
        saveJson(createJson("event", name, content));
    }

    private void includeError(TikTokErrorEvent event) {
        var exception = event.getException();
        var exceptionName = event.getException().getClass().getSimpleName();

        var sw = new StringWriter();
        var pw = new PrintWriter(sw);
        event.getException().printStackTrace(pw);
        var content = sw.toString();

        var json = createJson("error", exceptionName, content);
        if (exception instanceof TikTokLiveMessageException ex) {
            json.addProperty("message", ex.messageToBase64());
            json.addProperty("response", ex.webcastResponseToBase64());
        }
        saveJson(json);
    }

    private void saveJson(JsonObject jsonObject) {
        if (!collectorSettings.getFilter().apply(jsonObject)) {
            return;
        }
        try {
            File file = new File(fileSettings.getParentFile(), jsonObject.get("dataType").getAsString()+":"+jsonObject.get("dataTypeName").getAsString()+".txt");
            file.createNewFile();
            Files.writeString(file.toPath(), jsonObject.toString(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonObject createJson(String dataType, String dataTypeName, String content) {
        JsonObject data = new JsonObject();
        data.addProperty("session", sessionId);
        for (var entry : collectorSettings.getExtraFields().entrySet()) {
            if (entry.getValue() instanceof JsonElement element)
                data.add(entry.getKey(), element);
            else
                data.addProperty(entry.getKey(), entry.getValue().toString());
        }
        data.addProperty("tiktokUser", userName);
        data.addProperty("dataType", dataType);
        data.addProperty("dataTypeName", dataTypeName);
        data.addProperty("content", content);
        return data;
    }
}