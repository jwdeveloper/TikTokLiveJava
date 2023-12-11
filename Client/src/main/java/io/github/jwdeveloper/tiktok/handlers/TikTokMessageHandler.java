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
package io.github.jwdeveloper.tiktok.handlers;


import io.github.jwdeveloper.tiktok.data.dto.MessageMetaData;
import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketMessageEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketUnhandledMessageEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageException;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.mappers.TikTokGenericEventMapper;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.utils.Stopwatch;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;


public abstract class TikTokMessageHandler {

    private final Map<String, io.github.jwdeveloper.tiktok.handler.TikTokMessageHandler> handlers;
    private final TikTokEventObserver tikTokEventHandler;
    protected final TikTokGenericEventMapper mapper;

    public TikTokMessageHandler(TikTokEventObserver tikTokEventHandler, TikTokGenericEventMapper mapper) {
        handlers = new HashMap<>();
        this.tikTokEventHandler = tikTokEventHandler;
        this.mapper = mapper;
    }

    public void registerMapping(Class<?> clazz, Function<byte[], TikTokEvent> func) {
        handlers.put(clazz.getSimpleName(), messagePayload -> List.of(func.apply(messagePayload)));
    }

    public void registerMappings(Class<?> clazz, Function<byte[], List<TikTokEvent>> func) {
        handlers.put(clazz.getSimpleName(), func::apply);
    }

    public void registerMapping(Class<?> input, Class<?> output) {
        registerMapping(input, (e) -> mapper.mapToEvent(input, output, e));
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

    public void handleSingleMessage(LiveClient client, WebcastResponse.Message message) throws Exception {
        var messageClassName = message.getMethod();
        if (!handlers.containsKey(messageClassName)) {
            tikTokEventHandler.publish(client, new TikTokWebsocketUnhandledMessageEvent(message));
            return;
        }
        var handler = handlers.get(messageClassName);
        var stopwatch = new Stopwatch();
        stopwatch.start();
        var events = handler.handle(message.getPayload().toByteArray());
        var handlingTimeInMs = stopwatch.stop();
        var metadata = new MessageMetaData(Duration.ofNanos(handlingTimeInMs));

        for (var event : events) {
            tikTokEventHandler.publish(client, new TikTokWebsocketMessageEvent(message, event, metadata));
            tikTokEventHandler.publish(client, event);
        }
    }

}
