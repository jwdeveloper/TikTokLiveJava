package io.github.jwdeveloper.tiktok.listener;


import io.github.jwdeveloper.tiktok.annotations.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.TikTokEventConsumer;
import io.github.jwdeveloper.tiktok.exceptions.TikTokEventListenerMethodException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.live.LiveClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TikTokListenersManager implements ListenersManager {
    private final TikTokEventObserver eventObserver;
    private final List<ListenerBindingModel> bindingModels;

    public TikTokListenersManager(List<TikTokEventListener> listeners, TikTokEventObserver tikTokEventHandler) {
        this.eventObserver = tikTokEventHandler;
        this.bindingModels = listeners.stream().map(this::bindToEvents).toList();
    }


    @Override
    public List<TikTokEventListener> getBindingModels() {
        return bindingModels.stream().map(ListenerBindingModel::getListener).toList();
    }

    @Override
    public void addListener(TikTokEventListener listener) {
        var alreadyExists = bindingModels.stream().filter(e -> e.getListener() == listener).findAny();
        if (alreadyExists.isPresent()) {
            throw new TikTokLiveException("Listener " + listener.getClass() + " has already been registered");
        }
        var bindingModel = bindToEvents(listener);
        bindingModels.add(bindingModel);
    }

    @Override
    public void removeListener(TikTokEventListener listener) {
        var optional = bindingModels.stream().filter(e -> e.getListener() == listener).findAny();
        if (optional.isEmpty()) {
            return;
        }

        var bindingModel =optional.get();

        for(var consumer : bindingModel.getEvents())
        {
            eventObserver.unsubscribe(consumer);
        }
        bindingModels.remove(optional.get());
    }

    private ListenerBindingModel bindToEvents(TikTokEventListener listener) {

        var clazz = listener.getClass();
        var methods = Arrays.stream(clazz.getDeclaredMethods()).filter(m ->
                m.getParameterCount() == 2 &&
                        m.isAnnotationPresent(TikTokEventHandler.class) &&
                        m.getParameterTypes()[0].equals(LiveClient.class)).toList();
        var eventConsumer = new ArrayList<TikTokEventConsumer<?>>();


        for (var method : methods)
        {
            var eventClazz = method.getParameterTypes()[1];
            if(eventClazz.isAssignableFrom(TikTokEvent.class) && !eventClazz.equals(TikTokEvent.class))
            {
                throw new TikTokEventListenerMethodException("Method "+method.getName()+"() 2nd parameter must instance of "+TikTokEvent.class.getName());
            }
            var tikTokEventConsumer = new TikTokEventConsumer() {
                @Override
                public void onEvent(LiveClient liveClient, TikTokEvent event) {
                    try {
                        method.invoke(listener, liveClient, event);
                    } catch (Exception e) {
                        throw new TikTokEventListenerMethodException(e);
                    }
                }
            };
            eventObserver.subscribe(eventClazz, tikTokEventConsumer);
        }
        return new ListenerBindingModel(listener, eventConsumer);
    }
}
