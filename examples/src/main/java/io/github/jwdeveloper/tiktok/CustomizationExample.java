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

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.live.*;

/**
 * When the default implementation does not meet your needs,
 * you can override it using `customize` method
 */
public class CustomizationExample {
    public static void main(String[] args) {

        var customEventHandler = new CustomEventsHandler();
        var client = TikTokLive.newClient("john")
                .configure(liveClientSettings ->
                {
                    liveClientSettings.setFetchGifts(false);
                    liveClientSettings.setOffline(true);
                })
                .onError((liveClient, event) ->
                {
                    event.getException().printStackTrace();
                })
                .customize(container ->
                {
                    //overriding default implementation of LiveEventsHandler, with own one
                    container.registerSingleton(LiveEventsHandler.class, customEventHandler);
                }).build();

        client.connect();
        client.publishEvent(TikTokGiftEvent.of("rose", 1, 12));
        client.publishEvent(TikTokGiftEvent.of("stone", 2, 12));
    }


    public static class CustomEventsHandler extends TikTokLiveEventHandler {

        @Override
        public void publish(LiveClient tikTokLiveClient, TikTokEvent tikTokEvent) {
            System.out.println("Hello from custom events handler: " + tikTokEvent.getClass().getSimpleName());
        }
    }
}