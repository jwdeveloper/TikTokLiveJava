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
package io.github.jwdeveloper.tiktok.extension.collector;

import io.github.jwdeveloper.tiktok.extension.collector.api.settings.FileDataCollectorSettings;
import io.github.jwdeveloper.tiktok.extension.collector.api.settings.mongo.MongoDataCollectorSettings;
import io.github.jwdeveloper.tiktok.extension.collector.impl.*;
import io.github.jwdeveloper.tiktok.extension.collector.impl.storages.FileStorage;
import io.github.jwdeveloper.tiktok.extension.collector.impl.storages.MongoStorage;

import java.util.function.Consumer;


/**
 *
 */
public class TikTokLiveCollector
{

    public static DataCollector useMongo(Consumer<MongoDataCollectorSettings> consumer) {
        var settings = new MongoDataCollectorSettings();
        consumer.accept(settings);

        var storage = new MongoStorage(settings);
        return new DataCollector(storage);
    }

    public static DataCollector useFile(Consumer<FileDataCollectorSettings> consumer) {
        var settings = new FileDataCollectorSettings();
        consumer.accept(settings);

        var storage = new FileStorage(settings);
        return new DataCollector(storage);
    }
}