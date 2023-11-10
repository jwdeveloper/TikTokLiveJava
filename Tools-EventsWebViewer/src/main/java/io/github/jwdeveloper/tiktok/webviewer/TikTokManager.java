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
package io.github.jwdeveloper.tiktok.webviewer;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.tools.collector.client.MessageCollector;
import io.github.jwdeveloper.tiktok.tools.collector.client.TikTokMessageCollectorClient;
import io.github.jwdeveloper.tiktok.tools.collector.client.TikTokMessagessCollectorBuilder;
import io.github.jwdeveloper.tiktok.tools.util.MessageUtil;
import lombok.Value;

import java.sql.SQLException;
import java.util.Base64;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

public class TikTokManager {
    TikTokMessagessCollectorBuilder client;
    MessageCollector msgCollector;

    public TikTokManager() {
        msgCollector = new MessageCollector("ab");
    }

    public void connect(String name) throws SQLException {
        disconnect();
        client = TikTokMessageCollectorClient.create(msgCollector, "ab")
                .addOnBuilder(liveClientBuilder ->
                {

                    liveClientBuilder.onGift((liveClient, event) ->
                    {

                    });
                })
                .addUser(name);
        client.buildAndRun();
    }

    public List<String> getEventsNames() {
        return msgCollector.getMessages().keySet().stream().toList();
    }

    public List<String> getEventMessages(String eventName) {
        return msgCollector.getMessages().get(eventName).stream().map(MessageCollector.MessageData::getEventData).toList();
    }


    public MessageDto getMessage(String event, String index) throws InvalidProtocolBufferException {
        var eventData = msgCollector.getMessages().get(event);
        var messages = eventData.stream().toList();
        var random = new Random();

        var msgIndex = 0;
        if (index != null && !index.isEmpty()) {
            msgIndex = Integer.parseInt(index);
            msgIndex = Math.min(msgIndex, messages.size() - 1);
            msgIndex = Math.max(msgIndex, 0);
        }


        var msg = messages.get(msgIndex);


        var bytes = Base64.getDecoder().decode(msg.getEventData());
        var content = MessageUtil.getContent(event, bytes);
        return new MessageDto(content, msg.getEventData(), event);
    }


    public PagesDto getPages(String event) throws InvalidProtocolBufferException {
        var eventData = msgCollector.getMessages().get(event);
        var messages = eventData.stream().toList();

        var counter = new AtomicInteger(-1);
        var pages = messages.stream().map(e ->
        {
            return "http://localhost:8001/tiktok/events/message?eventName=" + event + "&page=" + counter.incrementAndGet();
        }).toList();

        return new PagesDto(event, messages.size(), pages);
    }

    @Value
    public class PagesDto {
        String eventName;
        int pages;
        List<String> links;
    }


    @Value
    public class MessageDto {
        String content;
        String base64;
        String eventName;
    }

    public void disconnect() {
        if (client == null) {
            return;
        }
        client.stop();
    }

}
