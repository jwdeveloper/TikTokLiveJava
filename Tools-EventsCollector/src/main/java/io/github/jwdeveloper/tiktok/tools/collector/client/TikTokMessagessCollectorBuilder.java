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

import io.github.jwdeveloper.tiktok.TikTokLiveClientBuilder;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.builder.LiveClientBuilder;
import io.github.jwdeveloper.tiktok.tools.collector.db.TikTokDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TikTokMessagessCollectorBuilder {
    List<String> users;
    String outputFileName;
    List<Class<?>> filters;
    Consumer<LiveClientBuilder> onBuilder;
    List<LiveClient> tiktokclients;

    MessageCollector messageCollector;

    public TikTokMessagessCollectorBuilder(String outputName) {
        users = new ArrayList<>();
        outputFileName = outputName;
        filters = new ArrayList<>();
        onBuilder = (e) -> {
        };
        tiktokclients = new ArrayList<>();
        messageCollector = new MessageCollector(outputName);
    }

    public TikTokMessagessCollectorBuilder(MessageCollector messageCollector, String outputFileName) {
        this(outputFileName);
        this.messageCollector = messageCollector;
    }

    public TikTokMessagessCollectorBuilder setOutputName(String name) {
        outputFileName = name;
        return this;
    }


    public TikTokMessagessCollectorBuilder addOnBuilder(Consumer<LiveClientBuilder> consumer) {
        onBuilder = consumer;
        return this;
    }

    public TikTokMessagessCollectorBuilder addUser(String user) {
        users.add(user);
        return this;
    }

    public TikTokMessagessCollectorBuilder addEventFilter(Class<?> event) {
        filters.add(event);
        return this;
    }

    public MessageCollector buildAndRun() throws SQLException {
        var db = new TikTokDatabase(outputFileName);
        db.init();
        var factory = new TikTokClientFactory(messageCollector, db);
        for (var user : users) {
            var client = factory.runClientAsync(user, onBuilder);
            client.thenAccept(liveClient ->
            {
                tiktokclients.add(liveClient);
            });
        }
        return messageCollector;
    }

    public void stop() {
        for (var client : tiktokclients) {
            client.disconnect();
        }
    }
}
