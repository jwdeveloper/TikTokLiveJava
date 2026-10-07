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
package io.github.jwdeveloper.tiktok.listener;

import io.github.jwdeveloper.tiktok.client.listener.TikTokListenersManager;
import io.github.jwdeveloper.dependance.Dependance;
import io.github.jwdeveloper.dependance.api.DependanceContainer;
import io.github.jwdeveloper.tiktok.client.TikTokLiveEventHandler;
import io.github.jwdeveloper.tiktok.api.annotations.Priority;
import io.github.jwdeveloper.tiktok.api.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.api.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.api.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.api.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.api.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.api.live.LiveClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

class TikTokListenersManagerTest {

    private TikTokLiveEventHandler eventObserver;
    private TikTokListenersManager tikTokListenersManager;
    private DependanceContainer dependanceContainer;
    private LiveClient liveClient;

    @BeforeEach
    void setUp() {

        liveClient = Mockito.mock(LiveClient.class);
        eventObserver = new TikTokLiveEventHandler();

        dependanceContainer = Dependance.newContainer()
                .registerSingleton(LiveClient.class, liveClient)
                .build();
        tikTokListenersManager = new TikTokListenersManager(eventObserver, dependanceContainer);
    }

    @AfterEach
    void stopListenerExecutor() throws ReflectiveOperationException, InterruptedException {
        var executor = getListenerExecutor();
        if (executor != null) {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS), "Listener executor did not terminate");
        }
    }

    @Test
    void synchronousListenersDoNotAllocateExecutor() throws ReflectiveOperationException {
        assertNull(getListenerExecutor());
        var listener = new SynchronousListener();
        tikTokListenersManager.addListener(listener);
        assertNull(getListenerExecutor());

        var event = new TikTokEvent() {};
        eventObserver.publish(liveClient, event);

        assertSame(event, listener.receivedEvent);
        assertSame(Thread.currentThread(), listener.callbackThread);
        assertNull(getListenerExecutor());
    }

    @Test
    void asynchronousListenerAllocatesCachedExecutorAndReceivesEvents()
            throws ReflectiveOperationException, InterruptedException {
        assertNull(getListenerExecutor());
        var listener = new AsynchronousListener();
        tikTokListenersManager.addListener(listener);

        var executor = getListenerExecutor();
        assertNotNull(executor);
        assertTrue(executor instanceof ThreadPoolExecutor);
        var cachedPool = (ThreadPoolExecutor) executor;
        assertEquals(0, cachedPool.getCorePoolSize());
        assertEquals(Integer.MAX_VALUE, cachedPool.getMaximumPoolSize());

        var event = new TikTokEvent() {};
        eventObserver.publish(liveClient, event);

        assertTrue(listener.completed.await(5, TimeUnit.SECONDS), "Async listener was not invoked");
        assertSame(event, listener.receivedEvent.get());
        assertNotSame(Thread.currentThread(), listener.callbackThread.get());
        assertTrue(cachedPool.getTaskCount() >= 1, "Callback did not use the listener executor");
        assertSame(executor, getListenerExecutor());
    }

    @Test
    void addListener() {
        Object listener = new TikTokEventListenerTest();
        tikTokListenersManager.addListener(listener);

        List<Object> listeners = tikTokListenersManager.getListeners();
        assertEquals(1, listeners.size());
        assertSame(listener, listeners.get(0));
    }

    @Test
    void addListener_alreadyRegistered_throwsException() throws ReflectiveOperationException {
        Object listener = new TikTokEventListenerTest();
        tikTokListenersManager.addListener(listener);
        var executor = getListenerExecutor();
        assertNotNull(executor);

        Exception exception = assertThrows(TikTokLiveException.class, () -> {
            tikTokListenersManager.addListener(listener);
        });

        assertEquals("Listener " + listener.getClass() + " has already been registered", exception.getMessage());
        assertSame(executor, getListenerExecutor());
    }

    @Test
    void removeListener() {
        Object listener = new TikTokEventListenerTest();
        tikTokListenersManager.addListener(listener);
        tikTokListenersManager.removeListener(listener);

        List<Object> listeners = tikTokListenersManager.getListeners();
        assertTrue(listeners.isEmpty());
    }

    @Test
    public void shouldTriggerEvents() {

        Object listener = new TikTokEventListenerTest();
        tikTokListenersManager.addListener(listener);


        var fakeGiftEvent = TikTokGiftEvent.of("TestRosa", 1, 1);
        eventObserver.publish(liveClient, fakeGiftEvent);
    }

    @Test
    void removeListener_notRegistered_doesNotThrow() {
        Object listener = new TikTokEventListenerTest();
        assertDoesNotThrow(() -> tikTokListenersManager.removeListener(listener));
    }

    private ExecutorService getListenerExecutor() throws ReflectiveOperationException {
        var field = TikTokListenersManager.class.getDeclaredField("executorService");
        field.setAccessible(true);
        return (ExecutorService) field.get(tikTokListenersManager);
    }

    public static class SynchronousListener {
        private TikTokEvent receivedEvent;
        private Thread callbackThread;

        @TikTokEventObserver
        public void onEvent(TikTokEvent event) {
            receivedEvent = event;
            callbackThread = Thread.currentThread();
        }
    }

    public static class AsynchronousListener {
        private final AtomicReference<TikTokEvent> receivedEvent = new AtomicReference<>();
        private final AtomicReference<Thread> callbackThread = new AtomicReference<>();
        private final CountDownLatch completed = new CountDownLatch(1);

        @TikTokEventObserver(async = true)
        public void onEvent(TikTokEvent event) {
            receivedEvent.set(event);
            callbackThread.set(Thread.currentThread());
            completed.countDown();
        }
    }


    public static class TikTokEventListenerTest {
        @TikTokEventObserver
        public void onJoin(LiveClient client, TikTokJoinEvent joinEvent) {
            System.out.println("Hello from on join" + client + " " + joinEvent);
        }

        @TikTokEventObserver(priority = Priority.LOWEST)
        public void onGift(LiveClient client, TikTokGiftEvent giftMessageEvent) {
            System.out.println("Hello from onGift lowest priority" + client + " " + giftMessageEvent);
        }

        @TikTokEventObserver(priority = Priority.NORMAL)
        public void onGift2(LiveClient client, TikTokGiftEvent giftMessageEvent) {
            System.out.println("Hello from onGift normal priority " + client + " " + giftMessageEvent);
        }

        @TikTokEventObserver(priority = Priority.HIGHEST)
        public void onGift3(LiveClient client, TikTokGiftEvent giftMessageEvent) {
            System.out.println("Hello from onGift highest priority " + client + " " + giftMessageEvent);
        }

        @TikTokEventObserver(async = true)
        public void onEvent(LiveClient client, TikTokEvent event) {
            System.out.println("Hello from onEvent im running on the thread " + Thread.currentThread().getName());
        }
    }
}