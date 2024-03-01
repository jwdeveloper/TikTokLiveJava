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
