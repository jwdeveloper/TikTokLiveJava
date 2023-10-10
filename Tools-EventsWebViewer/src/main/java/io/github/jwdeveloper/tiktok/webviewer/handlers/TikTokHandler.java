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
