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

import com.google.protobuf.GeneratedMessageV3;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.live.builder.LiveClientBuilder;
import io.github.jwdeveloper.tiktok.tools.collector.api.DataCollectorBuilder;
import io.github.jwdeveloper.tiktok.tools.collector.api.DataCollector;
import io.github.jwdeveloper.tiktok.tools.collector.api.TikTokDataCollectorModel;
import io.github.jwdeveloper.tiktok.tools.db.TikTokDatabase;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.function.Consumer;

public class TikTokDataCollectorBuilder implements DataCollectorBuilder {


    TikTokDataCollectorModel dataModel;

    TikTokDatabase database;

    public TikTokDataCollectorBuilder(String outputName) {

        dataModel = new TikTokDataCollectorModel();
        dataModel.setOutputName(outputName);
        dataModel.setUsers(new ArrayList<>());
        dataModel.setEventsFilter(new HashSet<>());
        dataModel.setMessagesFilter(new HashSet<>());
        dataModel.setOutputPath("...");
        dataModel.setOnConfigureLiveClient((e) -> {
        });
    }

    @Override
    public DataCollectorBuilder addUser(String user) {
        dataModel.getUsers().add(user);
        return this;
    }

    @Override
    public TikTokDataCollectorBuilder addMessageFilter(Class<? extends GeneratedMessageV3> message) {
        dataModel.getMessagesFilter().add(message.getSimpleName());
        return this;
    }

    @Override
    public TikTokDataCollectorBuilder addMessageFilter(String message) {
        dataModel.getMessagesFilter().add(message);
        return this;
    }

    @Override
    public TikTokDataCollectorBuilder addEventFilter(Class<? extends TikTokEvent> event) {
        dataModel.getEventsFilter().add(event.getSimpleName());
        return this;
    }

    @Override
    public TikTokDataCollectorBuilder addEventFilter(String event) {
        dataModel.getEventsFilter().add(event);
        return this;
    }


    @Override
    public DataCollectorBuilder setOutputPath(String path) {
        dataModel.setOutputPath(path);
        return this;
    }

    @Override
    public DataCollectorBuilder setSessionTag(String sessionTimestamp) {
        dataModel.setSessionTag(sessionTimestamp);
        return this;
    }

    @Override
    public DataCollectorBuilder setDatabase(TikTokDatabase database)
    {
        this.database =database;
        return this;
    }

    @Override
    public DataCollectorBuilder configureLiveClient(Consumer<LiveClientBuilder> consumer) {
        dataModel.setOnConfigureLiveClient(consumer);
        return this;
    }


    @Override
    public DataCollector buildAndRun() {

        var collector = build();
        collector.connect();
        return collector;
    }

    @Override
    public DataCollector build() {

        if (dataModel.getSessionTag().isEmpty()) {
            dataModel.setSessionTag(UUID.randomUUID().toString());
        }

        if(database == null)
        {
            database =  new TikTokDatabase(dataModel.getOutputName());
        }
        var dataCollector = new TikTokDataCollector(dataModel, database);
        return dataCollector;
    }


}
