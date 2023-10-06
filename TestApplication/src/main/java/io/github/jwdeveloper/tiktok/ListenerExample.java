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

import io.github.jwdeveloper.tiktok.annotations.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.listener.TikTokEventListener;
import io.github.jwdeveloper.tiktok.live.LiveClient;

import java.io.IOException;

public class ListenerExample {
    /*
      Listeners are an alternative way of handling events.
      I would to suggest to use then when logic of handing event
      is more complex
     */
    public static void main(String[] args) throws IOException {

        CustomListener customListener = new CustomListener();

        // set tiktok username
        var client = TikTokLive.newClient(Main.TEST_TIKTOK_USER)
                .addListener(customListener)
                .buildAndConnect();

        System.in.read();
    }

    /*
       Method in TikTokEventListener should meet 4 requirements to be detected
        - must have @TikTokEventHandler annotation
        - must have 2 parameters
        - first parameter must be LiveClient
        - second must be class that extending TikTokEvent
     */
    public static class CustomListener implements TikTokEventListener {

        @TikTokEventHandler
        public void onLike(LiveClient liveClient, TikTokLikeEvent event) {
            System.out.println(event.toString());
        }

        @TikTokEventHandler
        public void onError(LiveClient liveClient, TikTokErrorEvent event) {
            System.out.println(event.getException().getMessage());
        }

        @TikTokEventHandler
        public void onComment(LiveClient liveClient, TikTokCommentEvent event) {
            System.out.println(event.getText());
        }

        @TikTokEventHandler
        public void onGift(LiveClient liveClient, TikTokGiftEvent event) {
            var message = switch (event.getGift()) {
                case ROSE -> "Thanks :)";
                default -> "as";
            };


        }

        @TikTokEventHandler
        public void onAnyEvent(LiveClient liveClient, TikTokEvent event) {
            System.out.println(event.getClass().getSimpleName());
        }

    }
}
