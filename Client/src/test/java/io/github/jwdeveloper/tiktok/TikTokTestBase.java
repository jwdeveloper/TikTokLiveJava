package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.dependance.implementation.DependanceContainerBuilder;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveEventsHandler;
import io.github.jwdeveloper.tiktok.live.builder.LiveClientBuilder;
import io.github.jwdeveloper.tiktok.mocks.EventsHandlerMock;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.junit.Before;

/**
 * Base class for the unit tests
 */

@Getter
@Accessors(fluent = true)
public abstract class TikTokTestBase {

    private LiveClient liveClient;

    private EventsHandlerMock eventsHandlerMock;

    private TikTokRoomInfo roomInfoMock;

    public void AssertEvents(Class<? extends TikTokEvent>... events) {
        eventsHandlerMock.assertEvents(events);
    }

    @Before
    public void setup() {

        var builder = TikTokLive.newClient("test");
        eventsHandlerMock = new EventsHandlerMock();
        roomInfoMock = new TikTokRoomInfo();
        roomInfoMock.setHostName("test");
        liveClient = builder
                .configure(liveClientSettings ->
                {
                    liveClientSettings.setOffline(true);
                    liveClientSettings.setFetchGifts(false);
                })
                .customize(containerBuilder ->
                {
                    containerBuilder.registerSingleton(LiveEventsHandler.class, eventsHandlerMock);
                    containerBuilder.registerSingleton(TikTokRoomInfo.class, roomInfoMock);
                    onBeforeEachTest(builder, containerBuilder);
                }).build();
    }

    public abstract void onBeforeEachTest(LiveClientBuilder liveClientBuilder, DependanceContainerBuilder containerBuilder);
}
