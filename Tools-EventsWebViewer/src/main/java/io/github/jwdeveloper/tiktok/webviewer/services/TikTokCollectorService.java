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
package io.github.jwdeveloper.tiktok.webviewer.services;

import io.github.jwdeveloper.tiktok.tools.TikTokLiveTools;
import io.github.jwdeveloper.tiktok.tools.collector.api.DataCollector;
import io.github.jwdeveloper.tiktok.tools.db.TikTokDatabase;
import io.github.jwdeveloper.tiktok.webviewer.Settings;

public class TikTokCollectorService {
    private final Settings settings;
    private final TikTokDatabase database;

    private boolean isConnected = false;

    private DataCollector collector;

    public TikTokCollectorService(Settings settings, TikTokDatabase database) {
        this.settings = settings;
        this.database = database;
    }

    public void start(String user, String sessionTag) {
        stop();
        collector = createClient(user, sessionTag);

        collector.connect();
    }

    public boolean isRunning()
    {
        if(collector == null)
        {
            return false;
        }
        return isConnected;
    }

    public void stop() {
        if (collector != null) {
            collector.disconnect(true);
        }
        isConnected =false;
        collector = null;
        try {
            database.close();
            database.connect();
        }
        catch (Exception e)
        {
           e.printStackTrace();
        }

    }


    private DataCollector createClient(String user, String sessionTag) {
        return TikTokLiveTools.createCollector("dupa")
                .addUser(user)
                .setSessionTag(sessionTag)
                .setDatabase(database)
                .configureLiveClient(liveClientBuilder ->
                {
                    liveClientBuilder.configure(clientSettings ->
                    {
                        clientSettings.setPrintToConsole(true);
                    });

                    liveClientBuilder.onWebsocketResponse((liveClient, event) ->
                    {
                        for (var msg : event.getResponse().getMessagesList()) {
                            System.out.println(msg.getMethod());
                        }
                    });
                    liveClientBuilder.onDisconnected((liveClient, event) ->
                    {
                        liveClient.getLogger().info("Disconnected");
                        isConnected = false;
                    });
                    liveClientBuilder.onError((liveClient, event) ->
                    {
                        event.getException().printStackTrace();
                    });
                    liveClientBuilder.onConnected((liveClient, event) ->
                    {
                        liveClient.getLogger().info("Connected");
                        isConnected = true;
                    });
                }).build();
    }
}
