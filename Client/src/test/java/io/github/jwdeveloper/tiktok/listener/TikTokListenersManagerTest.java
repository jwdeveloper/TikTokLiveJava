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

import io.github.jwdeveloper.tiktok.annotations.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventObserver;
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

    private TikTokEventObserver eventObserver;
    private TikTokListenersManager tikTokListenersManager;

    @BeforeEach
    void setUp() {
        eventObserver = Mockito.mock(TikTokEventObserver.class);
        List<TikTokEventListener> listeners = new ArrayList<>();
        tikTokListenersManager = new TikTokListenersManager(listeners, eventObserver);
    }

    @Test
    void addListener() {
        TikTokEventListener listener =new TikTokEventListenerTest();
        tikTokListenersManager.addListener(listener);

        List<TikTokEventListener> listeners = tikTokListenersManager.getListeners();
        assertEquals(1, listeners.size());
        assertSame(listener, listeners.get(0));
    }

    @Test
    void addListener_alreadyRegistered_throwsException() {
        TikTokEventListener listener = new TikTokEventListenerTest();
        tikTokListenersManager.addListener(listener);

        Exception exception = assertThrows(TikTokLiveException.class, () -> {
            tikTokListenersManager.addListener(listener);
        });

        assertEquals("Listener " + listener.getClass() + " has already been registered", exception.getMessage());
    }

    @Test
    void removeListener() {
        TikTokEventListener listener = new TikTokEventListenerTest();
        tikTokListenersManager.addListener(listener);
        tikTokListenersManager.removeListener(listener);

        List<TikTokEventListener> listeners = tikTokListenersManager.getListeners();
        assertTrue(listeners.isEmpty());
    }

    @Test
    void removeListener_notRegistered_doesNotThrow() {
        TikTokEventListener listener = new TikTokEventListenerTest();
        assertDoesNotThrow(() -> tikTokListenersManager.removeListener(listener));
    }


    public static class TikTokEventListenerTest implements TikTokEventListener
    {
        @TikTokEventHandler
        public void onJoin(LiveClient client, TikTokJoinEvent joinEvent)
        {

        }

        @TikTokEventHandler
        public void onGift(LiveClient client, TikTokGiftEvent giftMessageEvent)
        {

        }

        @TikTokEventHandler
        public void onEvent(LiveClient client, TikTokEvent event)
        {

        }
    }
}