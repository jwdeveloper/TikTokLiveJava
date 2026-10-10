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

import io.github.jwdeveloper.tiktok.client.common.AsyncHandler;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class WebSocketHeartbeatTaskTest {
    private static final long INTERVAL = TimeUnit.DAYS.toMillis(1);

    @Test
    void neverOpenedSocketDoesNotScheduleHeartbeat() throws ReflectiveOperationException {
        var socket = mock(WebSocket.class);
        var heartbeat = new WebSocketHeartbeatTask();

        try {
            heartbeat.run(socket, INTERVAL);

            assertNull(state(heartbeat, "task"));
            assertNull(state(heartbeat, "webSocket"));
            verify(socket, never()).send(any(byte[].class));
        } finally {
            heartbeat.stop();
        }
    }

    @Test
    void closedSocketTickCancelsAndClearsHeartbeat() throws Exception {
        var socket = mock(WebSocket.class);
        var checks = new AtomicInteger();
        var closedTick = new CountDownLatch(1);
        when(socket.isOpen()).thenAnswer(invocation -> {
            if (checks.getAndIncrement() == 0)
                return true;
            closedTick.countDown();
            return false;
        });
        var heartbeat = new WebSocketHeartbeatTask();

        try {
            var future = start(heartbeat, socket);

            assertTrue(closedTick.await(5, TimeUnit.SECONDS));
            assertThrows(CancellationException.class, () -> future.get(5, TimeUnit.SECONDS));
            assertNull(state(heartbeat, "task"));
            assertNull(state(heartbeat, "webSocket"));
            assertCancelled(future);
            verify(socket, never()).send(any(byte[].class));
        } finally {
            heartbeat.stop();
        }
    }

    @Test
    void staleTickAndOldSocketStopCannotCancelReplacement() throws Exception {
        var oldSocket = mock(WebSocket.class);
        var newSocket = mock(WebSocket.class);
        var oldFirstTick = new CountDownLatch(1);
        var newFirstTick = new CountDownLatch(1);
        when(oldSocket.isOpen()).thenReturn(true);
        when(newSocket.isOpen()).thenReturn(true);
        doAnswer(invocation -> { oldFirstTick.countDown(); return null; })
            .when(oldSocket).send(any(byte[].class));
        doAnswer(invocation -> { newFirstTick.countDown(); return null; })
            .when(newSocket).send(any(byte[].class));
        var heartbeat = new WebSocketHeartbeatTask();

        try {
            var oldFuture = start(heartbeat, oldSocket);
            awaitFirstTick(oldFirstTick);
            var newFuture = start(heartbeat, newSocket);
            awaitFirstTick(newFirstTick);

            tick(heartbeat, oldSocket);
            heartbeat.stop(oldSocket);

            assertCancelled(oldFuture);
            assertFalse(newFuture.isCancelled());
            verify(oldSocket).send(any(byte[].class));
            verify(newSocket).send(any(byte[].class));
            assertSame(newFuture, state(heartbeat, "task"));
            assertSame(newSocket, state(heartbeat, "webSocket"));
        } finally {
            heartbeat.stop();
        }
    }

    @Test
    void explicitStopIsIdempotentAndMakesQueuedTickInert() throws Exception {
        var socket = mock(WebSocket.class);
        var firstTick = new CountDownLatch(1);
        when(socket.isOpen()).thenReturn(true);
        doAnswer(invocation -> { firstTick.countDown(); return null; })
            .when(socket).send(any(byte[].class));
        var heartbeat = new WebSocketHeartbeatTask();

        try {
            var future = start(heartbeat, socket);
            awaitFirstTick(firstTick);

            heartbeat.stop();
            heartbeat.stop();
            tick(heartbeat, socket);

            assertCancelled(future);
            verify(socket).send(any(byte[].class));
            assertNull(state(heartbeat, "task"));
            assertNull(state(heartbeat, "webSocket"));
        } finally {
            heartbeat.stop();
        }
    }

    private ScheduledFuture<?> start(WebSocketHeartbeatTask heartbeat, WebSocket socket)
        throws ReflectiveOperationException {
        synchronized (heartbeat) {
            heartbeat.run(socket, INTERVAL);
            return (ScheduledFuture<?>) state(heartbeat, "task");
        }
    }

    private void awaitFirstTick(CountDownLatch firstTick) throws Exception {
        assertTrue(firstTick.await(5, TimeUnit.SECONDS));
        // The shared scheduler has one worker; this barrier also waits for the tick to finish.
        var barrier = AsyncHandler.getHeartBeatScheduler().submit(() -> {});
        try {
            barrier.get(5, TimeUnit.SECONDS);
        } finally {
            barrier.cancel(false);
        }
    }

    private void assertCancelled(ScheduledFuture<?> future) {
        assertTrue(future.isCancelled());
        assertThrows(CancellationException.class, () -> future.get(5, TimeUnit.SECONDS));
        var scheduler = (ScheduledThreadPoolExecutor) AsyncHandler.getHeartBeatScheduler();
        assertFalse(scheduler.getQueue().contains(future));
    }

    private void tick(WebSocketHeartbeatTask heartbeat, WebSocket socket) throws ReflectiveOperationException {
        var method = WebSocketHeartbeatTask.class.getDeclaredMethod("sendHeartbeat", WebSocket.class);
        method.setAccessible(true);
        method.invoke(heartbeat, socket);
    }

    private Object state(WebSocketHeartbeatTask heartbeat, String name) throws ReflectiveOperationException {
        synchronized (heartbeat) {
            var field = WebSocketHeartbeatTask.class.getDeclaredField(name);
            field.setAccessible(true);
            return field.get(heartbeat);
        }
    }
}
