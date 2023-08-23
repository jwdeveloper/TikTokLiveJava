package io.github.jwdeveloper.tiktok.handlers;

import io.github.jwdeveloper.tiktok.TikTokLiveClient;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.TikTokEventConsumer;

import java.util.HashMap;
import java.util.Map;

public class TikTokEventHandler {
    private final Map<String, TikTokEventConsumer> events;

    public TikTokEventHandler() {
        events = new HashMap<>();
    }

    public void publish(TikTokLiveClient tikTokLiveClient, TikTokEvent tikTokEvent) {
        if (events.containsKey(TikTokEvent.class.getSimpleName())) {
            var handler = events.get(TikTokEvent.class.getSimpleName());
            handler.onEvent(tikTokLiveClient, tikTokEvent);
        }

        var name = tikTokEvent.getClass().getSimpleName();
        if (!events.containsKey(name)) {
            return;
        }
        var handler = events.get(name);
        handler.onEvent(tikTokLiveClient, tikTokEvent);
    }

    public <T extends TikTokEvent> void subscribe(Class<?> clazz, TikTokEventConsumer<T> event) {
        events.put(clazz.getSimpleName(), event);
    }

    public <T extends TikTokEvent> void unsubscribe(Class<?> clazz) {
        events.remove(clazz);
    }
}
