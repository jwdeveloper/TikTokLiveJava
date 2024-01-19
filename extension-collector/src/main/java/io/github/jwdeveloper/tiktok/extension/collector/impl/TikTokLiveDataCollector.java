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
package io.github.jwdeveloper.tiktok.extension.collector.impl;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import io.github.jwdeveloper.tiktok.extension.collector.api.data.CollectorListenerSettings;
import io.github.jwdeveloper.tiktok.extension.collector.api.data.LiveDataCollectorSettings;
import org.bson.Document;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

public class TikTokLiveDataCollector {

    private final LiveDataCollectorSettings settings;
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;

    public TikTokLiveDataCollector(LiveDataCollectorSettings settings) {
        this.settings = settings;
    }


    public void connectDatabase() {
        var serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        var mongoSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(settings.getConnectionUrl()))
                .serverApi(serverApi)
                .build();

        mongoClient = MongoClients.create(mongoSettings);
        database = mongoClient.getDatabase(settings.getDatabaseName());
        collection = database.getCollection("data");
        collection.createIndex(Indexes.hashed("session"));
        collection.createIndex(Indexes.hashed("dataType"));
    }


    public void disconnectDatabase() {
        mongoClient.close();
    }

    public TikTokLiveDataCollectorListener newListener() {
        return newListener(Map.of());
    }

    public TikTokLiveDataCollectorListener newListener(Map<String, Object> additionalFields) {
        return newListener(additionalFields, (e)->true);
    }

    public TikTokLiveDataCollectorListener newListener(Map<String, Object> additionalFields,
                                                       Function<Document, Boolean> filter) {
        var settings = new CollectorListenerSettings();
        settings.setExtraFields(additionalFields);
        settings.setFilter(filter);
        return new TikTokLiveDataCollectorListener(collection, settings);
    }
}
