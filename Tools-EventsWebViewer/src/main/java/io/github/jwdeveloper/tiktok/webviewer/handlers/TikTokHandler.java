package io.github.jwdeveloper.tiktok.webviewer.handlers;

import com.google.gson.Gson;
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.webviewer.TikTokManager;
import io.javalin.http.Context;

import java.sql.SQLException;

public class TikTokHandler {
    private final TikTokManager tikTokManager;

    public TikTokHandler(TikTokManager tikTokManager) {
        this.tikTokManager = tikTokManager;
    }


    public void connect(Context context) throws SQLException {
        String name = context.queryParam("name");
        if (name.equals(" ")) {
            context.result("Name can not be empty");
            context.status(400);
            return;
        }
        tikTokManager.connect(name);
        context.status(200);
    }

    public void disconnect(Context context) throws SQLException {
        tikTokManager.disconnect();
        context.status(200);
    }

    public void events(Context context) throws SQLException {
        var events = tikTokManager.getEventsNames();
        var gson = new Gson();
        var result = gson.toJson(events);
        context.result(result);
        context.status(200);
    }

    public void eventMessage(Context context) throws InvalidProtocolBufferException {
        String name = context.queryParam("eventName");
        var result = tikTokManager.getMessage(name);
        var gson = new Gson();
        context.result(gson.toJson(result));
    }
}
