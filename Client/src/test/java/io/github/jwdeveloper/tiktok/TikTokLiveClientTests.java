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
import io.github.jwdeveloper.tiktok.data.events.TikTokConnectedEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokDisconnectedEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.control.TikTokConnectingEvent;
import io.github.jwdeveloper.tiktok.data.events.control.TikTokPreConnectionEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomInfoEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.live.builder.LiveClientBuilder;
import io.github.jwdeveloper.tiktok.models.ConnectionState;
import org.junit.Assert;
import org.junit.Test;

public class TikTokLiveClientTests extends TikTokTestBase {

    @Override
    public void onBeforeEachTest(LiveClientBuilder liveClientBuilder,
                                         DependanceContainerBuilder containerBuilder) {


    }

    @Test
    public void shouldThrownWhenAlreadyConnected() {
        roomInfoMock().setConnectionState(ConnectionState.CONNECTED);
        Assert.assertThrows(TikTokLiveException.class, () ->
        {
            liveClient().connect();
        });
        Assert.assertEquals(ConnectionState.DISCONNECTED, roomInfoMock().getConnectionState());
        AssertEvents(
                TikTokErrorEvent.class,
                TikTokDisconnectedEvent.class
        );
    }

    @Test
    public void shouldConnect() {
        liveClient().connect();
        Assert.assertEquals(ConnectionState.CONNECTED, roomInfoMock().getConnectionState());
        AssertEvents(
                TikTokConnectingEvent.class,
                TikTokPreConnectionEvent.class,
                TikTokConnectedEvent.class,
                TikTokRoomInfoEvent.class);
    }
}
