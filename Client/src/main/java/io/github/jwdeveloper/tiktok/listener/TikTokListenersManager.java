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


import io.github.jwdeveloper.tiktok.TikTokLiveEventHandler;
import io.github.jwdeveloper.tiktok.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokEventListenerMethodException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.builder.EventConsumer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class TikTokListenersManager implements ListenersManager {
    private final TikTokLiveEventHandler eventObserver;
    private final List<ListenerBindingModel> bindingModels;

    public TikTokListenersManager(List<TikTokEventListener> listeners, TikTokLiveEventHandler tikTokEventHandler) {
        this.eventObserver = tikTokEventHandler;
        this.bindingModels = new ArrayList<>(listeners.size());
        for (var listener : listeners) {
            addListener(listener);
        }
    }

    @Override
    public List<TikTokEventListener> getListeners() {
        return bindingModels.stream().map(ListenerBindingModel::getListener).toList();
    }

    @Override
    public void addListener(TikTokEventListener listener) {
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
    public void removeListener(TikTokEventListener listener) {
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

    private ListenerBindingModel bindToEvents(TikTokEventListener listener) {

        var clazz = listener.getClass();
        var methods = Arrays.stream(clazz.getDeclaredMethods()).filter(m ->
                m.getParameterCount() == 2 &&
                        m.isAnnotationPresent(TikTokEventObserver.class)).toList();
        var eventsMap = new HashMap<Class<?>, List<EventConsumer<?>>>();
        for (var method : methods) {
            var liveclientClass = method.getParameterTypes()[0];
            var eventClass = method.getParameterTypes()[1];

            if (!LiveClient.class.isAssignableFrom(liveclientClass) && !liveclientClass.equals(LiveClient.class)) {
                throw new TikTokEventListenerMethodException("Method " + method.getName() + "() 1st parameter must be instance of " + LiveClient.class.getName()
                    + " | Invalid parameter class: "+liveclientClass.getName());
            }

            if (!TikTokEvent.class.isAssignableFrom(eventClass) && !eventClass.equals(TikTokEvent.class)) {
                throw new TikTokEventListenerMethodException("Method " + method.getName() + "() 2nd parameter must be instance of " + TikTokEvent.class.getName()
                    + " | Invalid parameter class: "+eventClass.getName());
            }

            EventConsumer eventMethodRef = (liveClient, event) ->
            {
                try {
                    method.setAccessible(true);
                    method.invoke(listener, liveClient, event);
                } catch (Exception e) {
                    throw new TikTokEventListenerMethodException(e);
                }
            };
            eventsMap.computeIfAbsent(eventClass, (a) -> new ArrayList<>()).add(eventMethodRef);
        }
        return new ListenerBindingModel(listener, eventsMap);
    }
}