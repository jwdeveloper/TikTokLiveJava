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
