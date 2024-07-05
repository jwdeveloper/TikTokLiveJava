package io.github.jwdeveloper.tiktok.mocks;

import io.github.jwdeveloper.tiktok.TikTokLiveEventHandler;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.live.LiveClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Cache published events,
 */
public class EventsHandlerMock extends TikTokLiveEventHandler {
    private final List<TikTokEvent> publishedEvents = new ArrayList<TikTokEvent>();


    @Override
    public void publish(LiveClient tikTokLiveClient, TikTokEvent tikTokEvent) {
        super.publish(tikTokLiveClient, tikTokEvent);
        publishedEvents.add(tikTokEvent);
    }

    @SafeVarargs
    public final void assertEvents(Class<? extends TikTokEvent>... events) {

        if (events.length == 0 && !publishedEvents.isEmpty()) {
            var classNames = publishedEvents.stream()
                    .map(e -> e.getClass().getSimpleName())
                    .toList();
            var invokedEvents = String.join("\n", classNames);
            throw new IllegalArgumentException("Not events should be invoked but there was: \n" + invokedEvents);
        }


        for (var i = 0; i < events.length; i++) {
            var expectedEvent = events[i];
            var invokedEvent = publishedEvents.get(i);
            if (expectedEvent.equals(invokedEvent.getClass())) {
                continue;
            }
            throw new RuntimeException("Expected event was " + expectedEvent + " but acctuall was " + invokedEvent.getClass());
        }


    }
}
