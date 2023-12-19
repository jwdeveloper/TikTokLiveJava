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
package io.github.jwdeveloper.tiktok.tools.tester;

import com.google.gson.JsonParser;
import io.github.jwdeveloper.tiktok.tools.tester.mockClient.TikTokLiveMock;
import io.github.jwdeveloper.tiktok.tools.tester.mockClient.mocks.LiveClientMock;
import io.github.jwdeveloper.tiktok.tools.util.MessageUtil;

import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

public class RunJsonTester {


    public static void main(String[] args) throws IOException {
        var messages = getMessages();
        var client =(LiveClientMock) TikTokLiveMock.create()
                .onWebsocketUnhandledMessage((liveClient, event) ->
                {
                    var sb = new StringBuilder();
                    sb.append("Unhandled Message! " );
                    sb.append(event.getData().getMethod());
                    sb.append("\n");
                    sb.append(MessageUtil.getContent(event.getData()));


                   // liveClient.getLogger().info(sb.toString());
                }).
                onGift((liveClient, event) ->
                {
                    liveClient.getLogger().info("Gift event: "+event.toJson());
                })
                .onGiftCombo((liveClient, event) ->
                {
                    liveClient.getLogger().info("GiftCombo event"+event.toJson());
                })
                .onError((liveClient, event) ->
                {
                    event.getException().printStackTrace();
                })
                .build();
        for(var msg : messages.entrySet())
        {
            for(var content : msg.getValue())
            {
                client.publishMessage(msg.getKey(),content);
            }
        }
        client.connect();
    }


    private static Map<String, List<String>> getMessages() throws IOException {
        var path = "C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Tools-EventsCollector\\src\\main\\resources\\log.json";
        var jsonElement = JsonParser.parseReader(new FileReader(path, Charset.defaultCharset()));

        var res = new HashMap<String, List<String>>();
        if (jsonElement.isJsonObject()) {
            var jsonObject = jsonElement.getAsJsonObject();
            var keys = jsonObject.keySet();
            for (String key : keys) {
                var messages = jsonObject.get(key).getAsJsonArray();
                for (var msg : messages) {
                    var data = msg.getAsJsonObject().get("eventData").getAsString();
                    res.computeIfAbsent(key, s -> new ArrayList<>()).add(data);
                }
            }
        }
        return res;
    }
}
