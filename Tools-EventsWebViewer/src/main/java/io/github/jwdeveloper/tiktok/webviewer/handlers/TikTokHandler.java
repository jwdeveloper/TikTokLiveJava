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
package io.github.jwdeveloper.tiktok.webviewer.handlers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.webviewer.Settings;
import io.github.jwdeveloper.tiktok.webviewer.services.TikTokCollectorService;
import io.github.jwdeveloper.tiktok.webviewer.services.TikTokDatabaseService;
import io.javalin.http.Context;
import lombok.Value;

import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class TikTokHandler {
    private final TikTokDatabaseService databaseService;
    private final Settings settings;
    private final TikTokCollectorService collectorService;

    public TikTokHandler(TikTokDatabaseService databaseService,
                         Settings settings,
                         TikTokCollectorService collectorService) {
        this.databaseService = databaseService;
        this.settings = settings;
        this.collectorService = collectorService;
    }


    public void connect(Context context) {
        var name = context.queryParam("name");
        var sessionTag = context.queryParam("session");
        System.out.println("Session tag" + sessionTag);
        collectorService.start(name, sessionTag);
        settings.setUserName(name);
        context.status(200);
    }

    public void connectionStatus(Context context) {
        var isWorking = collectorService.isRunning();
        var result = getGson().toJson(isWorking);
        context.result(result);
        context.status(200);
    }

    public void disconnect(Context context) {

        collectorService.stop();
        context.status(200);
    }


    public void getUsers(Context context) {
        var users = databaseService.getUsers();
        var result = getGson().toJson(users);
        context.result(result);
        context.status(200);
    }

    public void getUserSessionTags(Context context) {
        var dataType = context.queryParam("user");
        var sessionsTags = databaseService.getSessionTag(dataType);

        var result = getGson().toJson(sessionsTags);
        context.result(result);
        context.status(200);
    }

    public void getDataTypes(Context context) {
        var result = getGson().toJson(List.of("event", "message", "response"));
        context.result(result);
        context.status(200);
    }


    public void updateSearch(Context context) {
        var userName = context.queryParam("user");
        var sessionTag = context.queryParam("session");

        settings.setUserName(userName);
        settings.setSessionTag(sessionTag);
        context.status(200);
    }


    public void getDataNames(Context context) {
        var dataType = context.queryParam("type");
        var dataNames = databaseService.getDataNames(dataType, settings.getUserName(), settings.getSessionTag());
        var gson = getGson();


        var result = gson.toJson(dataNames);
        context.result(result);
        context.status(200);
    }

    public void getData(Context context) {
        var page = context.queryParam("page");
        var dataType = context.queryParam("type");
        var dataName = context.queryParam("name");
        if (page == null) {
            page = "0";
        }


        var asProto = context.queryParam("asProto");
        var asJson = asProto == null;
        var dto = new TikTokDatabaseService.DatabaseDataDto(dataType, dataName, settings.getUserName(), settings.getSessionTag(), asJson);
        var result = databaseService.getData(dto);
        var content = result.get(Integer.parseInt(page));

        var response = new MessageDto(content, "", dataName);
        var gson = getGson();
        context.result(gson.toJson(response));
    }

    public void getDataPages(Context context) throws InvalidProtocolBufferException {
        var dataType = context.queryParam("type");
        var dataName = context.queryParam("name");

        var asJson = true;
        var dto = new TikTokDatabaseService.DatabaseDataDto(dataType, dataName, settings.getUserName(), settings.getSessionTag(), asJson);
        var result = databaseService.getData(dto);
        var counter = new AtomicInteger(-1);
        var pages = result.stream().map(e ->
        {
            return "http://localhost:" + settings.getPort() + "/tiktok/data?type=" + dataType + "&page=" + counter.incrementAndGet() + "&name=" + dataName;
        }).toList();

        var response = new PagesDto(dataName, counter.get(), pages);
        var gson = getGson();
        context.result(gson.toJson(response));
    }


    public Gson getGson() {
        return new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();
    }


    @Value
    public class PagesDto {
        String eventName;
        int pages;
        List<String> links;
    }


    @Value
    public class MessageDto {
        String content;
        String base64;
        String eventName;
    }
}
