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
