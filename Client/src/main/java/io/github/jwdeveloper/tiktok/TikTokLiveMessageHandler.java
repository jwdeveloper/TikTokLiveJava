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
package io.github.jwdeveloper.tiktok;


import io.github.jwdeveloper.tiktok.data.dto.MessageMetaData;
import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketMessageEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketUnhandledMessageEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageException;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveEventsHandler;
import io.github.jwdeveloper.tiktok.live.LiveMessagesHandler;
import io.github.jwdeveloper.tiktok.mappers.LiveMapper;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.utils.Stopwatch;

import java.time.Duration;

public class TikTokLiveMessageHandler implements LiveMessagesHandler {

    private final LiveEventsHandler tikTokEventHandler;
    private final LiveMapper mapper;

    public TikTokLiveMessageHandler(LiveEventsHandler tikTokEventHandler, LiveMapper mapper) {
        this.tikTokEventHandler = tikTokEventHandler;
        this.mapper = mapper;
    }

    public void handle(LiveClient client, WebcastResponse webcastResponse) {
        tikTokEventHandler.publish(client, new TikTokWebsocketResponseEvent(webcastResponse));
        for (var message : webcastResponse.getMessagesList()) {
            try {
                handleSingleMessage(client, message);
            } catch (Exception e) {
                var exception = new TikTokLiveMessageException(message, webcastResponse, e);
                tikTokEventHandler.publish(client, new TikTokErrorEvent(exception));
            }
        }
    }

    public void handleSingleMessage(LiveClient client, WebcastResponse.Message message) {
        var messageClassName = message.getMethod();
        if (!mapper.isRegistered(messageClassName)) {
            tikTokEventHandler.publish(client, new TikTokWebsocketUnhandledMessageEvent(message));
            return;
        }

        var stopwatch = new Stopwatch();
        stopwatch.start();
        var events = mapper.handleMapping(messageClassName, message.getPayload().toByteArray());
        var handlingTimeInMs = stopwatch.stop();
        var metadata = new MessageMetaData(Duration.ofNanos(handlingTimeInMs));

        for (var event : events) {
            tikTokEventHandler.publish(client, new TikTokWebsocketMessageEvent(message, event, metadata));
            tikTokEventHandler.publish(client, event);
        }
    }
}