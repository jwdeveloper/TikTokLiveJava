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

import io.github.jwdeveloper.tiktok.utils.ConsoleColors;

import java.io.IOException;

public class SimpleExample {
    public static void main(String[] args) throws IOException {

        // set tiktok username
        TikTokLive.newClient(Main.TEST_TIKTOK_USER)
                .configure(clientSettings ->
                {

                })
                .onGift((liveClient, event) ->
                {
                    switch (event.getGift()) {
                        case ROSE ->
                                print("\uD83D\uDC95", ConsoleColors.YELLOW, "x", event.getCombo(), " roses!", "\uD83D\uDC95");
                        default ->
                                print(ConsoleColors.GREEN, "[Thanks for gift] ",ConsoleColors.YELLOW, event.getGift().getName(), "X", event.getCombo());
                    }
                })
                .onConnected((client, event) ->
                {
                    print(ConsoleColors.GREEN, "[Connected]");
                })
                .onFollow((liveClient, event) ->
                {
                    print(ConsoleColors.BLUE, "Follow -> ", ConsoleColors.WHITE_BRIGHT, event.getUser().getName());
                })
                .onJoin((client, event) ->
                {
                    print(ConsoleColors.GREEN, "Join -> ", ConsoleColors.WHITE_BRIGHT, event.getUser().getName());
                })
                .onComment((client, event) ->
                {
                    print(ConsoleColors.WHITE, event.getUser().getName(), ":", ConsoleColors.WHITE_BRIGHT, event.getText());
                })
                .onEvent((client, event) ->
                {
                    //System.out.println("Event: " +event.getClass().getSimpleName());
                })
                .onError((client, event) ->
                {
                    event.getException().printStackTrace();
                })
                .buildAndConnectAsync();

        System.in.read();
    }

    private static void print(Object... messages) {
        var sb = new StringBuilder();
        for (var message : messages) {
            sb.append(message).append(" ");
        }
        System.out.println(sb);
    }
}
