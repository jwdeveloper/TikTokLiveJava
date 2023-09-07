package io.github.jwdeveloper.tiktok.handlers;

import io.github.jwdeveloper.tiktok.TikTokLiveClient;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.TikTokEventConsumer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TikTokEventObserver {
    private final Map<String, Set<TikTokEventConsumer>> events;

    public TikTokEventObserver() {
        events = new HashMap<>();
    }

    public void publish(TikTokLiveClient tikTokLiveClient, TikTokEvent tikTokEvent) {
        if (events.containsKey(TikTokEvent.class.getSimpleName())) {
            var handlers = events.get(TikTokEvent.class.getSimpleName());
            for(var handle : handlers)
            {
                handle.onEvent(tikTokLiveClient, tikTokEvent);
            }
        }

        var name = tikTokEvent.getClass().getSimpleName();
        if (!events.containsKey(name)) {
            return;
        }
        var handlers = events.get(name);
        for(var handler : handlers)
        {
            handler.onEvent(tikTokLiveClient, tikTokEvent);
        }
    }

    public <T extends TikTokEvent> void subscribe(Class<?> clazz, TikTokEventConsumer<T> event)
    {
        events.computeIfAbsent(clazz.getSimpleName(), e -> new HashSet<>()).add(event);
    }

    public <T extends TikTokEvent> void unsubscribeAll(Class<?> clazz) {
        events.remove(clazz);
    }
}
