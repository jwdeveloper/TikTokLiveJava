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

import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.utils.FilesUtility;
import io.github.jwdeveloper.tiktok.utils.JsonUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.File;
import java.lang.reflect.Type;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.*;
import java.util.logging.Logger;

public class MessagesManager {

    @Getter
    Map<String, Queue<MessageData>> messages;
    String outputName;

    int limit = 20;
    public MessagesManager(String outputName) {
        this.messages = new TreeMap<>();
        this.outputName = outputName;
        load();
    }

    public void addMessage(Logger logger, String host, WebcastResponse.Message message) {
        var name = message.getMethod();
        var payload = message.getPayload().toByteArray();
        var base64 = Base64.getEncoder().encodeToString(payload);

        if (!messages.containsKey(name)) {
            logger.info("New Message found! " + name);
            messages.put(name, new LinkedList<>());
        }

        var queue = messages.get(name);
        if (queue.size() > limit) {
            queue.remove();
        }

        queue.add(new MessageData(base64, host, LocalDateTime.now().toString()));
        save();
    }

    public String toJson() {
        return JsonUtil.toJson(messages);
    }

    public void load() {
        var file = new File(path());
        Type type = new TypeToken<Map<String, Queue<MessageData>>>() {}.getType();

        if (file.exists()) {
            var content = FilesUtility.loadFileContent(path());
            var gson = new GsonBuilder().create();
            messages =  gson.fromJson(content,type);
        }
    }

    public void save() {

        FilesUtility.saveFile(path(), toJson());
    }

    public String path() {
        return Paths.get("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Tools-EventsCollector\\src\\main\\resources", outputName + ".json").toString();
    }

    @AllArgsConstructor
    @Getter
    public class MessageData {
        String eventData;
        String uniqueId;
        String ts;
    }
}
