package io.github.jwdeveloper.tiktok.websocket;

import io.github.jwdeveloper.tiktok.TikTokLiveEventHandler;
import io.github.jwdeveloper.tiktok.data.events.TikTokConnectedEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokDisconnectedEvent;
import io.github.jwdeveloper.tiktok.data.requests.LiveConnectionData;
import io.github.jwdeveloper.tiktok.live.LiveClient;

public class TikTokWebSocketOfflineClient implements SocketClient {

    private final TikTokLiveEventHandler handler;
    private LiveClient liveClient;

    public TikTokWebSocketOfflineClient(TikTokLiveEventHandler handler) {
        this.handler = handler;
    }

    @Override
    public void start(LiveConnectionData.Response webcastResponse, LiveClient tikTokLiveClient) {
        liveClient = tikTokLiveClient;
        handler.publish(liveClient, new TikTokConnectedEvent());
    }

    @Override
    public void stop() {
        if (liveClient == null) {
            return;
        }
        handler.publish(liveClient, new TikTokDisconnectedEvent());
    }
}
