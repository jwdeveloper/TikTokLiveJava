package io.github.jwdeveloper.tiktok.live;

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.live.builder.EventConsumer;

import java.util.HashSet;
import java.util.Optional;

public interface LiveEventsHandler {
    void publish(LiveClient tikTokLiveClient, TikTokEvent tikTokEvent);

    <T extends TikTokEvent> void subscribe(Class<?> clazz, EventConsumer<T> event);

    <T extends TikTokEvent> void unsubscribeAll(Class<?> clazz);

    <T extends TikTokEvent> void unsubscribe(EventConsumer<T> consumer);

    <T extends TikTokEvent> void unsubscribe(Class<?> clazz, EventConsumer<T> consumer);
}
