package io.github.jwdeveloper.tiktok.extension.collector.impl.storages;

import io.github.jwdeveloper.tiktok.extension.collector.api.Storage;
import io.github.jwdeveloper.tiktok.extension.collector.api.settings.FileDataCollectorSettings;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;

public class FileStorage implements Storage {

    private final FileDataCollectorSettings settings;

    public FileStorage(FileDataCollectorSettings fileDataCollectorSettings) {
        this.settings = fileDataCollectorSettings;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void insert(Document document) {
        var fileName = document.get("dataType") + "_" + document.get("dataTypeName") + ".json";
        try {
            var file = new File(settings.getParentFile(), fileName);
            file.createNewFile();
            Files.writeString(file.toPath(), document.toJson(JsonWriterSettings.builder().indent(true).build()), StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
