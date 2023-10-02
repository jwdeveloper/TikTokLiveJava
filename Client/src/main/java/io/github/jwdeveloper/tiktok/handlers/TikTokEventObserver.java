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

import io.github.jwdeveloper.tiktok.TikTokLiveClient;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.TikTokEventConsumer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TikTokEventObserver {
    private final Map<Class<?>, Set<TikTokEventConsumer>> events;

    public TikTokEventObserver() {
        events = new HashMap<>();
    }

    public void publish(TikTokLiveClient tikTokLiveClient, TikTokEvent tikTokEvent) {
        if (events.containsKey(TikTokEvent.class)) {
            var handlers = events.get(TikTokEvent.class);
            for (var handle : handlers) {
                handle.onEvent(tikTokLiveClient, tikTokEvent);
            }
        }


        if (!events.containsKey(tikTokEvent.getClass())) {
            return;
        }
        var handlers = events.get(tikTokEvent.getClass());
        for (var handler : handlers) {
            handler.onEvent(tikTokLiveClient, tikTokEvent);
        }
    }

    public <T extends TikTokEvent> void subscribe(Class<?> clazz, TikTokEventConsumer<T> event) {
        events.computeIfAbsent(clazz, e -> new HashSet<>()).add(event);
    }

    public <T extends TikTokEvent> void unsubscribeAll(Class<?> clazz) {
        events.remove(clazz);
    }

    public <T extends TikTokEvent> void unsubscribe(TikTokEventConsumer<T> consumer) {
        for (var entry : events.entrySet()) {
            entry.getValue().remove(consumer);
        }
    }

    public <T extends TikTokEvent> void unsubscribe(Class<?> clazz, TikTokEventConsumer<T> consumer) {
        if (clazz == null) {
            return;
        }

        if (!events.containsKey(clazz)) {
            return;
        }

        var eventSet = events.get(clazz);
        eventSet.remove(consumer);

    }
}
