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

import io.github.jwdeveloper.tiktok.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.utils.ConsoleColors;

import java.io.IOException;

public class ListenerExample {
    // <code>

    /**
     * Listeners are an alternative way of handling events.
     * I would to suggest to use then when logic of handing event
     * is more complex
     */
    public static void main(String[] args) throws IOException {
        showLogo();
        CustomListener customListener = new CustomListener();

        TikTokLive.newClient(ConnectionExample.TIKTOK_HOSTNAME)
                .addListener(customListener)
                .buildAndConnect();
        System.in.read();
    }

    /**
     * Method must meet 2 requirements to be detected
     * - must have @TikTokEventObserver annotation
     * - must have 1 parameter of type that extending TikTokEvent
     */

    public static class CustomListener {

        @TikTokEventObserver
        public void onLike(TikTokLikeEvent event) {
            System.out.println(event.toString());
        }

        @TikTokEventObserver
        public void onError(TikTokErrorEvent event, LiveClient liveClient) {
            //  event.getException().printStackTrace();
        }

        @TikTokEventObserver
        public void onComment(LiveClient liveClient, TikTokCommentEvent event) {
            var userName = event.getUser().getName();
            var text = event.getText();
            liveClient.getLogger().info(userName + ": " + text);
        }

        @TikTokEventObserver
        public void onGift(LiveClient liveClient, TikTokGiftEvent event) {
            var message = switch (event.getGift().getName()) {
                case "ROSE" -> "Thanks :)";
                case "APPETIZERS" -> ":OO";
                case "APRIL" -> ":D";
                case "TIKTOK" -> ":P";
                case "CAP" -> ":F";
                default -> ":I";
            };
            liveClient.getLogger().info(message);
        }

        @TikTokEventObserver
        public void onAnyEvent(LiveClient liveClient, TikTokEvent event) {
            liveClient.getLogger().info(event.getClass().getSimpleName());
        }

    }

    // </code>
    private static void showLogo() {
        System.out.println(ConsoleColors.GREEN + """
                                
                 _____ _ _    _____     _    _     _          \s
                |_   _(_) | _|_   _|__ | | _| |   (_)_   _____\s
                  | | | | |/ / | |/ _ \\| |/ / |   | \\ \\ / / _ \\
                  | | | |   <  | | (_) |   <| |___| |\\ V /  __/ 
                  |_| |_|_|\\_\\ |_|\\___/|_|\\_\\_____|_| \\_/ \\___| 
                """);

    }
}