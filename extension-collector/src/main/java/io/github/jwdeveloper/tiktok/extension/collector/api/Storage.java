package io.github.jwdeveloper.tiktok.extension.collector.api;

import org.bson.Document;

public interface Storage {
    void connect();

    void disconnect();

    void insert(Document document);
}
