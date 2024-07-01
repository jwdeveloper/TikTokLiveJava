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
package io.github.jwdeveloper.tiktok.extension.collector.impl.storages;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;
import io.github.jwdeveloper.tiktok.extension.collector.api.Storage;
import io.github.jwdeveloper.tiktok.extension.collector.api.settings.mongo.MongoDataCollectorSettings;
import org.bson.Document;

public class MongoStorage implements Storage {
    private MongoClient mongoClient;
    private MongoDatabase database;
    private MongoCollection<Document> collection;
    private final MongoDataCollectorSettings settings;

    public MongoStorage(MongoDataCollectorSettings settings) {
        this.settings = settings;
    }

    @Override
    public void connect() {

        var serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();
        var mongoSettings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(settings.getConnectionUrl()))
                .serverApi(serverApi)
                .build();

        mongoClient = MongoClients.create(mongoSettings);
        database = mongoClient.getDatabase(settings.getDatabaseName());
        collection = database.getCollection(settings.getCollectionName());
        collection.createIndex(Indexes.hashed("session"));
        collection.createIndex(Indexes.hashed("dataType"));
    }

    @Override
    public void disconnect() {

        if (mongoClient == null) {
            return;
        }
        mongoClient.close();
    }


    @Override
    public void insert(Document document) {
        collection.insertOne(document);
    }
}
