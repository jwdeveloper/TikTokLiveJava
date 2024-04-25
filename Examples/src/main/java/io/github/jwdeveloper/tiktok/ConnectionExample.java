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

import io.github.jwdeveloper.tiktok.data.events.TikTokSubNotifyEvent;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.envelop.TikTokChestEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.live.GiftsManager;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import io.github.jwdeveloper.tiktok.utils.ConsoleColors;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;

public class ConnectionExample {
    public static String TIKTOK_HOSTNAME = "kvadromama_marina1";

    public static void main(String[] args) throws IOException {

        showLogo();

        GiftsManager gifts = TikTokLive.gifts();

        TikTokLive.newClient(ConnectionExample.TIKTOK_HOSTNAME)
                .configure(clientSettings ->
                {
                    clientSettings.setHostName(ConnectionExample.TIKTOK_HOSTNAME); // This method is useful in case you want change hostname later
                    clientSettings.setClientLanguage("en"); // Language
                    clientSettings.setLogLevel(Level.ALL); // Log level
                    clientSettings.setPrintToConsole(true); // Printing all logs to console even if log level is Level.OFF
                    clientSettings.setRetryOnConnectionFailure(true); // Reconnecting if TikTok user is offline
                    clientSettings.setRetryConnectionTimeout(Duration.ofSeconds(1)); // Timeout before next reconnection


                    clientSettings.getHttpSettings();

                    //Optional: Sometimes not every message from chat are send to TikTokLiveJava to fix this issue you can set sessionId
                    // documentation how to obtain sessionId https://github.com/isaackogan/TikTok-Live-Connector#send-chat-messages

                    // clientSettings.setSessionId("86c3c8bf4b17ebb2d74bb7fa66fd0000");

                    //Optional:
                    //RoomId can be used as an override if you're having issues with HostId.
                    //You can find it in the HTML for the livestream-page

                    //clientSettings.setRoomId("XXXXXXXXXXXXXXXXX");
                })
                .onWebsocketMessage((liveClient, event) ->
                {


                    TikTokEvent tiktokLiveEvent = event.getEvent();
                    if (tiktokLiveEvent instanceof TikTokSubNotifyEvent) {
                        System.out.println("it was subscrible event");
                    }

                })
                .onEvent((liveClient, event) ->
                {
                    if (event instanceof TikTokGiftEvent) {
                        System.out.println("1");
                    }
                    if (event instanceof TikTokChestEvent) {
                        System.out.println("2");
                    }
                })
                .onGift((liveClient, event) ->
                {
                    switch (event.getGift().getName()) {
                        case "ROSE":
                            print(ConsoleColors.RED, "Rose!");
                            break;
                        case "GG":
                            print(ConsoleColors.YELLOW, " GOOD GAME!");
                            break;
                        case "TIKTOK":
                            print(ConsoleColors.CYAN, "Thanks for TikTok");
                            break;
                        default:
                            print(ConsoleColors.GREEN, "[Thanks for gift] ", ConsoleColors.YELLOW, event.getGift().getName(), "x", event.getCombo());
                    }
                })
                .onGiftCombo((liveClient, event) ->
                {
                    print(ConsoleColors.RED, "GIFT COMBO", event.getGift().getName(), event.getCombo());
                })
                .onConnected((liveClient, event) ->
                {
                    print(ConsoleColors.GREEN, "[Connected]");
                })
                .onDisconnected((liveClient, event) ->
                {
                    print(ConsoleColors.RED, "[Disconnected]");
                })
                .onRoomInfo((liveClient, event) ->
                {
                    LiveRoomInfo info = event.getRoomInfo();
                })
                .onFollow((liveClient, event) ->
                {
                    print(ConsoleColors.BLUE, "Follow:", ConsoleColors.WHITE_BRIGHT, event.getUser().getName());
                })
                .onJoin((liveClient, event) ->
                {
                    print(ConsoleColors.WHITE, "Join:", ConsoleColors.WHITE_BRIGHT, event.getUser().getName());
                })
                .onComment((liveClient, event) ->
                {
                    print(ConsoleColors.GREEN, event.getUser().getName(), ":", ConsoleColors.WHITE_BRIGHT, event.getText());
                })
                .onEvent((liveClient, event) ->
                {
                    //System.out.println("Event: " +event.getClass().getSimpleName());
                })
                .onError((liveClient, event) ->
                {
                    event.getException().printStackTrace();
                })
                .buildAndConnectAsync();
        System.in.read();
    }

    private static void print(Object... messages) {
        StringBuilder sb = new StringBuilder();
        for (Object message : messages) {
            sb.append(message).append(" ");
        }
        System.out.println(sb);
    }

    private static void showLogo() {
        System.out.println(ConsoleColors.GREEN + "\"" +
                "\n" +
                " _____ _ _    _____     _    _     _          \n" +
                "|_   _(_) | _|_   _|__ | | _| |   (_)_   _____\n" +
                "  | | | | |/ / | |/ _ \\| |/ / |   | \\ \\ / / _ \\\n" +
                "  | | | |   <  | | (_) |   <| |___| |\\ V /  __/\n" +
                "  |_| |_|_|\\_\\ |_|\\___/|_|\\_\\_____|_| \\_/ \\___|\n" +
                "\"");
    }
}
