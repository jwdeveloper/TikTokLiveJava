package io.github.jwdeveloper.tiktok.webviewer;

import io.github.jwdeveloper.tiktok.webviewer.handlers.TikTokHandler;
import io.javalin.Javalin;

public class Main {
    public static void main(String[] args) {

        var manager = new TikTokManager();
        var app = Javalin.create(config ->
        {
            config.plugins.enableCors(corsContainer ->
            {
                corsContainer.add(corsPluginConfig ->
                {
                    corsPluginConfig.allowHost("http://localhost:5500");
                });
            });
            config.staticFiles.add("/public");
        }).start(8001);

        var handler = new TikTokHandler(manager);
        app.get("/tiktok/connect", handler::connect);
        app.get("/tiktok/disconnect", handler::disconnect);
        app.get("/tiktok/events", handler::events);
        app.get("/tiktok/events/message", handler::eventMessage);
    }
}
