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
package io.github.jwdeveloper.tiktok.listener;

import io.github.jwdeveloper.dependance.Dependance;
import io.github.jwdeveloper.dependance.api.DependanceContainer;
import io.github.jwdeveloper.tiktok.TikTokLiveEventHandler;
import io.github.jwdeveloper.tiktok.annotations.Priority;
import io.github.jwdeveloper.tiktok.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

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

    @Test
    void addListener() {
        Object listener = new TikTokEventListenerTest();
        tikTokListenersManager.addListener(listener);

        List<Object> listeners = tikTokListenersManager.getListeners();
        assertEquals(1, listeners.size());
        assertSame(listener, listeners.get(0));
    }

    @Test
    void addListener_alreadyRegistered_throwsException() {
        Object listener = new TikTokEventListenerTest();
        tikTokListenersManager.addListener(listener);

        Exception exception = assertThrows(TikTokLiveException.class, () -> {
            tikTokListenersManager.addListener(listener);
        });

        assertEquals("Listener " + listener.getClass() + " has already been registered", exception.getMessage());
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