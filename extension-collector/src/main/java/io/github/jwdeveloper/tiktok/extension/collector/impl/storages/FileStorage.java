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
            var fileName = document.get("dataType") + "_" + document.get("dataTypeName") + (settings.isAppendUserName() ? "_"+document.getString("tiktokUser") : "") + ".json";
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