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


import io.github.jwdeveloper.dependance.api.DependanceContainer;
import io.github.jwdeveloper.tiktok.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokEventListenerMethodException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.live.LiveEventsHandler;
import io.github.jwdeveloper.tiktok.live.builder.EventConsumer;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class TikTokListenersManager implements ListenersManager {

    private final Map<Object, List<ListenerMethodInfo>> listeners;
    private final LiveEventsHandler eventsHandler;
    private final ExecutorService executorService;
    private final DependanceContainer dependanceContainer;


    public TikTokListenersManager(LiveEventsHandler tikTokEventHandler,
                                  DependanceContainer dependanceContainer) {
        this.eventsHandler = tikTokEventHandler;
        this.dependanceContainer = dependanceContainer;
        this.listeners = new HashMap<>();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    }

    @Override
    public List<Object> getListeners() {
        return listeners.keySet().stream().toList();
    }

    @Override
    public void addListener(Object listener) {
        if (listeners.containsKey(listener)) {
            throw new TikTokLiveException("Listener " + listener.getClass() + " has already been registered");
        }

        var methodsInfo = getMethodsInfo(listener);
        for (var methodInfo : methodsInfo) {
            eventsHandler.subscribe(methodInfo.getEventType(), methodInfo.getAction());
        }
        listeners.put(listener, methodsInfo);
    }

    @Override
    public void removeListener(Object listener) {
        if (!listeners.containsKey(listener)) {
            return;
        }
        var methodsInfo = listeners.get(listener);
        for (var methodInfo : methodsInfo) {
            eventsHandler.unsubscribe(methodInfo.getEventType(), methodInfo.getAction());
        }
        listeners.remove(listener);
    }

    private List<ListenerMethodInfo> getMethodsInfo(Object listener) {
        return Arrays.stream(listener.getClass().getDeclaredMethods())
                .filter(e -> e.isAnnotationPresent(TikTokEventObserver.class))
                .filter(e -> e.getParameterCount() >= 1)
                .map(method -> getSingleMethodInfo(listener, method))
                .sorted(Comparator.comparingInt(a -> a.getPriority().value))
                .toList();
    }

    private ListenerMethodInfo getSingleMethodInfo(Object listener, Method method) {

        method.setAccessible(true);
        var annotation = method.getAnnotation(TikTokEventObserver.class);
        var tiktokEventType = Arrays.stream(method.getParameterTypes())
                .filter(TikTokEvent.class::isAssignableFrom)
                .findFirst()
                .orElseThrow(() -> new TikTokEventListenerMethodException("Method " + method.getName() + "() must have only one parameter that inherits from class " + TikTokEvent.class.getName()));

        var info = new ListenerMethodInfo();
        info.setListener(listener);
        info.setAsync(annotation.async());
        info.setPriority(annotation.priority());
        info.setEventType(tiktokEventType);
        info.setAction(createAction(listener, method, tiktokEventType));

        if (info.isAsync()) {
            var action = info.getAction();
            info.setAction((liveClient, event) ->
            {
                executorService.submit(() ->
                {
                    action.onEvent(liveClient, event);
                });
            });
        }
        return info;
    }


    //I know, implementation of this might look complicated
    private EventConsumer createAction(Object listener, Method method, Class tiktokEventType) {
        AtomicReference<Object> eventObjectRef = new AtomicReference<>();
        var methodContainer = dependanceContainer.createChildContainer()
                .configure(configuration ->
                {
                    //Modifying container, so it returns TikTokEvent object instance,
                    //when TikTokEvent type is encountered in the methods parameters
                    configuration.onInjection(injectionEvent ->
                    {
                        if (injectionEvent.input().isAssignableFrom(tiktokEventType)) {
                            return eventObjectRef.get();
                        }
                        return injectionEvent.output();
                    });
                })
                .build();

        return (liveClient, event) ->
        {
            try {
                eventObjectRef.set(event);
                //Creating list of input objects based on method parameters
                //Objects are received from container
                var parameters = methodContainer.resolveParameters(method);
                method.invoke(listener, parameters);
            } catch (Exception e) {
                eventsHandler.publish(liveClient, new TikTokErrorEvent(new TikTokEventListenerMethodException(e)));
            }
        };
    }
}