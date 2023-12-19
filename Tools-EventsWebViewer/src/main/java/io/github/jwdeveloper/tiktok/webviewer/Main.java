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
package io.github.jwdeveloper.tiktok.webviewer;

import io.github.jwdeveloper.tiktok.tools.db.TikTokDatabase;
import io.github.jwdeveloper.tiktok.webviewer.handlers.TikTokHandler;
import io.github.jwdeveloper.tiktok.webviewer.services.TikTokCollectorService;
import io.github.jwdeveloper.tiktok.webviewer.services.TikTokDatabaseService;
import io.javalin.Javalin;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

public class Main {
    public static void main(String[] args) throws SQLException, ExecutionException, InterruptedException, IOException {
        var settings = new Settings();
        settings.setUserName("szalonamoniaxx");
        settings.setSessionTag("battle");
        settings.setDbName("db-battle");
        settings.setPort(8002);

        var db = new TikTokDatabase(settings.getDbName());
        db.connect();

        var service = new TikTokDatabaseService(db);
        var collectorService = new TikTokCollectorService(settings, db);
        var handler = new TikTokHandler(service, settings, collectorService);
        //  var manager = new TikTokManager(service);
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
        }).start(settings.getPort());

        app.get("/tiktok/status", handler::connectionStatus);
        app.get("/tiktok/connect", handler::connect);
        app.get("/tiktok/disconnect", handler::disconnect);

        app.get("/tiktok/data/pages", handler::getDataPages);
        app.get("/tiktok/data/names", handler::getDataNames);
        app.get("/tiktok/data", handler::getData);

        app.get("/tiktok/update", handler::updateSearch);
        app.get("/tiktok/sessions", handler::getUserSessionTags);
        app.get("/tiktok/users", handler::getUsers);
        app.get("/tiktok/data-types", handler::getDataTypes);
    }
}
