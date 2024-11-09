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
