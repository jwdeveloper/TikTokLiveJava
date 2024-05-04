package io.github.jwdeveloper.tiktok.extension.collector.impl.storages;

import io.github.jwdeveloper.tiktok.extension.collector.api.Storage;
import io.github.jwdeveloper.tiktok.extension.collector.api.settings.FileDataCollectorSettings;
import org.bson.Document;
import org.bson.json.JsonWriterSettings;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.*;

public class FileStorage implements Storage {

    private final FileDataCollectorSettings settings;
    private final Map<String, ReentrantLock> locks;

    public FileStorage(FileDataCollectorSettings fileDataCollectorSettings) {
        this.settings = fileDataCollectorSettings;
        this.locks = settings.isUseFileLocks() ? new ConcurrentHashMap<>() : null;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void insert(Document document) {
        if (settings.getTypeFilter().test(document.getString("dataType"), document.getString("dataTypeName")) && settings.getUserFilter().test(document.getString("tiktokUser"))) {
            var fileName = document.get("dataType") + "_" + document.get("dataTypeName") + (settings.isAppendUserName() ? document.getString("tiktokUser") : "") + ".json";
            if (settings.isUseFileLocks()) {
                var lock = locks.computeIfAbsent(fileName, s -> new ReentrantLock());
                lock.lock();
                save(document, fileName);
                lock.unlock();
            } else
                save(document, fileName);
        }
    }

    private void save(Document document, String fileName) {
        try {
            var file = new File(settings.getParentFile(), fileName);
            file.createNewFile();
            Files.writeString(file.toPath(), document.toJson(JsonWriterSettings.builder().indent(true).build())+'\n', StandardOpenOption.APPEND);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}