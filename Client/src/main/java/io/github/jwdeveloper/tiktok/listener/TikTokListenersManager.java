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
package io.github.jwdeveloper.tiktok.listener;


import io.github.jwdeveloper.tiktok.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokEventListenerMethodException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveEventsHandler;
import io.github.jwdeveloper.tiktok.live.builder.EventConsumer;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TikTokListenersManager implements ListenersManager {
    private final LiveEventsHandler eventObserver;
    private final List<ListenerBindingModel> bindingModels;
    private final ExecutorService executorService;

    public TikTokListenersManager(List<Object> listeners, LiveEventsHandler tikTokEventHandler) {
        this.eventObserver = tikTokEventHandler;
        this.bindingModels = new ArrayList<>(listeners.size());
        for (var listener : listeners) {
            addListener(listener);
        }
        executorService = Executors.newFixedThreadPool(4);
    }

    @Override
    public List<Object> getListeners() {
        return bindingModels.stream().map(ListenerBindingModel::getListener).toList();
    }

    @Override
    public void addListener(Object listener) {
        var alreadyExists = bindingModels.stream().filter(e -> e.getListener() == listener).findAny();
        if (alreadyExists.isPresent()) {
            throw new TikTokLiveException("Listener " + listener.getClass() + " has already been registered");
        }
        var bindingModel = bindToEvents(listener);

        for (var eventEntrySet : bindingModel.getEvents().entrySet()) {
            var eventType = eventEntrySet.getKey();
            for (var methods : eventEntrySet.getValue()) {
                eventObserver.subscribe(eventType, methods);
            }
        }
        bindingModels.add(bindingModel);
    }

    @Override
    public void removeListener(Object listener) {
        var optional = bindingModels.stream().filter(e -> e.getListener() == listener).findAny();
        if (optional.isEmpty()) {
            return;
        }

        var bindingModel = optional.get();

        for (var eventEntrySet : bindingModel.getEvents().entrySet()) {
            var eventType = eventEntrySet.getKey();
            for (var methods : eventEntrySet.getValue()) {
                eventObserver.unsubscribe(eventType, methods);
            }
        }
        bindingModels.remove(optional.get());
    }

    private ListenerBindingModel bindToEvents(Object listener) {
        var clazz = listener.getClass();
        var methods = Arrays.stream(clazz.getDeclaredMethods())
                .filter(m ->
                        m.getParameterCount() >= 1 &&
                                m.isAnnotationPresent(TikTokEventObserver.class))
                .toList();
        var eventsMap = new HashMap<Class<?>, List<EventConsumer<?>>>();
        for (var method : methods) {
            var annotation = method.getAnnotation(TikTokEventObserver.class);
            var tiktokEventsParameters = Arrays.stream(method.getParameters())
                    .filter(parameter ->
                            TikTokEvent.class.isAssignableFrom(parameter.getType()) ||
                                    parameter.getType().equals(TikTokEvent.class))
                    .toList();
            if (tiktokEventsParameters.size() != 1) {
                throw new TikTokEventListenerMethodException("Method " + method.getName() + "() must have only one parameter that inherits from class " + TikTokEvent.class.getName());
            }

            var eventType = tiktokEventsParameters.get(0).getType();
            EventConsumer eventMethodRef = (liveClient, event) ->
            {
                if (annotation.async()) {
                    executorService.submit(() ->
                    {
                        try {
                            method.setAccessible(true);
                            method.invoke(listener, liveClient, event);
                        } catch (Exception e) {
                            throw new TikTokEventListenerMethodException(e);
                        }
                    });
                    return;
                }

                try {
                    method.setAccessible(true);
                    method.invoke(listener, liveClient, event);
                } catch (Exception e) {
                    throw new TikTokEventListenerMethodException(e);
                }
            };
            eventsMap.computeIfAbsent(eventType, (a) -> new ArrayList<>()).add(eventMethodRef);
        }
        return new ListenerBindingModel(listener, eventsMap);
    }
}