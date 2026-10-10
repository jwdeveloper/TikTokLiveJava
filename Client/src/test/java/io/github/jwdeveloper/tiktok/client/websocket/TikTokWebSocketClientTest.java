/*
 * Copyright (c) 2023-2024 jwdeveloper jacekwoln@gmail.com
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
package io.github.jwdeveloper.tiktok.client.websocket;

import io.github.jwdeveloper.tiktok.api.data.events.TikTokConnectedEvent;
import io.github.jwdeveloper.tiktok.api.data.events.TikTokDisconnectedEvent;
import io.github.jwdeveloper.tiktok.api.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.api.http.LiveHttpClient;
import io.github.jwdeveloper.tiktok.api.listener.ListenersManager;
import io.github.jwdeveloper.tiktok.api.live.GiftsManager;
import io.github.jwdeveloper.tiktok.api.live.LiveClient;
import io.github.jwdeveloper.tiktok.api.live.LiveEventsHandler;
import io.github.jwdeveloper.tiktok.api.live.LiveMessagesHandler;
import io.github.jwdeveloper.tiktok.api.models.ConnectionState;
import io.github.jwdeveloper.tiktok.api.websocket.LiveClientStopType;
import io.github.jwdeveloper.tiktok.client.TikTokLiveClient;
import io.github.jwdeveloper.tiktok.client.TikTokRoomInfo;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TikTokWebSocketClientTest {
    private LiveClientSettings settings;
    private LiveMessagesHandler messages;
    private LiveEventsHandler events;
    private LiveClient liveClient;
    private WebSocketHeartbeatTask heartbeat;
    private TikTokWebSocketClient client;

    @BeforeEach
    void setUp() {
        settings = LiveClientSettings.createDefault();
        settings.setPingInterval(1000);
        messages = mock(LiveMessagesHandler.class);
        events = mock(LiveEventsHandler.class);
        liveClient = mock(LiveClient.class);
        heartbeat = mock(WebSocketHeartbeatTask.class);
        client = newClient(heartbeat);
    }

    @Test
    void currentOpenCallbackStartsHeartbeatAndPublishesConnectedEvent() throws ReflectiveOperationException {
        var listener = new ControlledListener(messages, events, liveClient, client);
        setSocket(client, listener);

        listener.onOpen(null);

        verify(heartbeat).run(listener, settings.getPingInterval());
        verify(events).publish(same(liveClient), any(TikTokConnectedEvent.class));
        assertEquals(1, listener.pings);
        assertTrue(client.isConnected());
    }

    @Test
    void staleOpenAndCloseCallbacksLeaveReplacementConnected() throws ReflectiveOperationException {
        var replacement = mock(WebSocketClient.class);
        when(replacement.isOpen()).thenReturn(true);
        var stale = new ControlledListener(messages, events, liveClient, client);
        setSocket(client, replacement);
        assertTrue(client.onOpen(replacement));

        stale.onOpen(null);
        stale.onClose(CloseFrame.NORMAL, "old socket", true);

        assertEquals(1, stale.closes);
        assertEquals(0, stale.pings);
        assertSame(replacement, socket(client));
        assertTrue(client.isConnected());
        verify(heartbeat).run(replacement, settings.getPingInterval());
        verify(heartbeat, never()).run(same(stale), anyLong());
        verify(heartbeat).stop(stale);
        verify(heartbeat, never()).stop();
        verifyNoInteractions(liveClient, events);
    }

    @Test
    void stoppingAlreadyClosedSocketStillCancelsHeartbeatAndDetaches() throws Exception {
        var socket = mock(WebSocketClient.class);
        when(socket.isClosed()).thenReturn(true);
        setSocket(client, socket);

        client.stop(LiveClientStopType.CLOSE_BLOCKING);

        verify(heartbeat).stop();
        assertNull(socket(client));
        verify(socket, never()).close();
        verify(socket, never()).close(anyInt(), anyString());
        verify(socket, never()).closeBlocking();
    }

    @Test
    void stoppingBeforeHandshakeCancelsHeartbeatWithoutBlocking() throws Exception {
        var socket = mock(WebSocketClient.class);
        setSocket(client, socket);

        client.stop(LiveClientStopType.CLOSE_BLOCKING);

        verify(heartbeat).stop();
        assertNull(socket(client));
        verify(socket).close(CloseFrame.NORMAL, "");
        verify(socket, never()).closeBlocking();
        verify(socket, never()).closeConnection(anyInt(), anyString());
    }

    @Test
    void liveClientDisconnectStopsHeartbeatEvenWhenSocketIsAlreadyClosed() throws Exception {
        var closedSocket = mock(WebSocketClient.class);
        when(closedSocket.isClosed()).thenReturn(true);
        setSocket(client, closedSocket);
        var room = mock(TikTokRoomInfo.class);
        var gifts = mock(GiftsManager.class);
        var http = mock(LiveHttpClient.class);
        var listeners = mock(ListenersManager.class);
        var logger = mock(Logger.class);
        var live = new TikTokLiveClient(messages, gifts, room, http, client, events,
            settings, listeners, logger);
        assertFalse(client.isConnected());

        live.disconnect();

        assertNull(socket(client));
        verify(room).setConnectionState(ConnectionState.DISCONNECTED);
        verify(heartbeat).stop();
        verify(closedSocket, never()).close();
        verifyNoInteractions(http, gifts, listeners, events);
    }

    @Test
    void closeCancelsAndDetachesBeforeCallbacksAndPreservesCallbackReplacement() throws Exception {
        var replacement = mock(WebSocketClient.class);
        when(replacement.isOpen()).thenReturn(true);
        var listener = new ControlledListener(messages, events, liveClient, client);
        setSocket(client, listener);
        assertTrue(client.onOpen(listener));
        listener.open = false;
        doAnswer(invocation -> {
            verify(heartbeat).stop(listener);
            assertNull(socket(client));
            assertFalse(client.isConnected());
            return null;
        }).when(liveClient).disconnect();
        doAnswer(invocation -> {
            setSocket(client, replacement);
            assertTrue(client.onOpen(replacement));
            return null;
        }).when(events).publish(same(liveClient), any(TikTokDisconnectedEvent.class));

        listener.onClose(CloseFrame.NORMAL, "closed", true);

        var order = inOrder(heartbeat, liveClient, events);
        order.verify(heartbeat).stop(listener);
        order.verify(liveClient).disconnect();
        order.verify(events).publish(same(liveClient), any(TikTokDisconnectedEvent.class));
        order.verify(heartbeat).run(replacement, settings.getPingInterval());
        assertSame(replacement, socket(client));
        assertTrue(client.isConnected());
        verify(heartbeat, never()).stop();
    }

    @Test
    void disconnectedCallbackExceptionCannotPreventHeartbeatCleanup() throws Exception {
        var listener = new ControlledListener(messages, events, liveClient, client);
        setSocket(client, listener);
        var failure = new IllegalStateException("End-user callback failed");
        doThrow(failure).when(events).publish(same(liveClient), any(TikTokDisconnectedEvent.class));

        assertSame(failure, assertThrows(IllegalStateException.class,
            () -> listener.onClose(CloseFrame.NORMAL, "closed", true)));

        assertNull(socket(client));
        var order = inOrder(heartbeat, liveClient, events);
        order.verify(heartbeat).stop(listener);
        order.verify(liveClient).disconnect();
        order.verify(events).publish(same(liveClient), any(TikTokDisconnectedEvent.class));
    }

    @Test
    void closeAfterExplicitStopPublishesEventWithoutDisconnectingAgain() throws Exception {
        var listener = new ControlledListener(messages, events, liveClient, client);
        setSocket(client, listener);
        client.stop(LiveClientStopType.NORMAL);

        listener.onClose(CloseFrame.NORMAL, "stopped", false);

        assertNull(socket(client));
        verifyNoInteractions(liveClient);
        verify(events).publish(same(liveClient), any(TikTokDisconnectedEvent.class));
    }

    private TikTokWebSocketClient newClient(WebSocketHeartbeatTask task) {
        return new TikTokWebSocketClient(settings, messages, events, task);
    }

    private void setSocket(TikTokWebSocketClient owner, WebSocketClient socket) throws ReflectiveOperationException {
        var field = TikTokWebSocketClient.class.getDeclaredField("webSocketClient");
        field.setAccessible(true);
        field.set(owner, socket);
    }

    private WebSocketClient socket(TikTokWebSocketClient owner) throws ReflectiveOperationException {
        var field = TikTokWebSocketClient.class.getDeclaredField("webSocketClient");
        field.setAccessible(true);
        return (WebSocketClient) field.get(owner);
    }

    private static final class ControlledListener extends TikTokWebSocketListener {
        private boolean open = true;
        private int closes;
        private int pings;

        private ControlledListener(LiveMessagesHandler messages, LiveEventsHandler events,
                                   LiveClient liveClient, TikTokWebSocketClient owner) {
            super(URI.create("ws://127.0.0.1/events"), Map.of(), 0, messages, events, liveClient, owner);
        }

        @Override public boolean isOpen() { return open; }
        @Override public void close() { closes++; }
        @Override public void sendPing() { pings++; }
    }
}
