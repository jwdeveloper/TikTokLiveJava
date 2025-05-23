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
package io.github.jwdeveloper.tiktok;


import io.github.jwdeveloper.tiktok.extension.collector.TikTokLiveCollector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CollectorExample {

    public static void main(String[] args) throws IOException {

        var path = "C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Examples\\src\\main\\resources";
        var collector = TikTokLiveCollector.useFile(settings ->
        {
            settings.setParentFile(new File(path));
        });
        collector.connect();

        var users = List.of("tehila_723", "dino123597", "domaxyzx", "dash4214", "obserwacje_live");
        Map<String, Object> additionalDataFields = Map.of("sessionTag", "ExampleTag");
        for (var user : users) {
            TikTokLive.newClient(user)
                    .configure(liveClientSettings ->
                    {
                        liveClientSettings.setPrintToConsole(true);
                    })
                    .onError((liveClient, event) ->
                    {
                        event.getException().printStackTrace();
                    })
                    .addListener(collector.newListener(additionalDataFields))
                    .buildAndConnectAsync();
        }

        System.in.read();
        collector.disconnect();
    }
}