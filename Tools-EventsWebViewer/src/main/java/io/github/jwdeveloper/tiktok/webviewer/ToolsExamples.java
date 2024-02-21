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
package io.github.jwdeveloper.tiktok.webviewer;

import io.github.jwdeveloper.tiktok.tools.TikTokLiveTools;

import java.io.IOException;

public class ToolsExamples {

    private static final String tiktokUser = "k.peaks";

    private static final String db = "a";

    private static final String sessionTag = "a";

    public static void main(String[] args) throws IOException {
        // runCollector();
        runCollector();
        //runTester();
        System.in.read();
    }

    /*
      WebcastLinkMicArmies
            WebcastLinkMicBattle
     */
    //WebcastLinkMicArmies battle data?
    //WebcastLinkMicBattlePunishFinish end of battle?
    //WebcastLinkLayerMessage send after end of battle
    // send after LinkLayer -> WebcastLinkMessage

    private static void runCollector() {
        TikTokLiveTools.createCollector(db)
                .addUser(tiktokUser)
                .setSessionTag(sessionTag)
                .configureLiveClient(liveClientBuilder ->
                {
                    liveClientBuilder.configure(clientSettings ->
                            {
                                clientSettings.setPrintToConsole(true);
                            })
                            .onComment((liveClient, event) ->
                            {
                                System.out.println("Chat message: " + event.getUser().getName() + " " + event.getText());
                            })
                            .onWebsocketUnhandledMessage((liveClient, event) ->
                            {
                                liveClient.getLogger().info(event.getMessage().getMethod());
                            });
                    liveClientBuilder.onConnected((liveClient, event) ->
                    {
                        liveClient.getLogger().info("Connected");
                    });
                }).buildAndRun();
    }

    private static void runTester() {
        TikTokLiveTools.createTester(db)
                .setSessionTag(sessionTag)
                .setUser(tiktokUser)
                .configureLiveClient(liveClientBuilder ->
                {
                    liveClientBuilder.onError((liveClient, event) ->
                    {
                        event.getException().printStackTrace();
                        ;
                    });

                    liveClientBuilder.onWebsocketResponse((liveClient, event) ->
                    {
                        System.out.println("Response =====================================");
                        for (var msg : event.getResponse().getMessagesList()) {
                            System.out.println("Message -> " + msg.getMethod());
                        }
                    });
                    liveClientBuilder.onEvent((liveClient, event) ->
                    {
                        System.out.println("Event -> " + event.getClass().getSimpleName());
                    });
                })
                .buildAndRun();
    }
}
