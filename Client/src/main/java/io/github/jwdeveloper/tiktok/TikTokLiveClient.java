package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.http.TikTokApiService;
import io.github.jwdeveloper.tiktok.live.ConnectionState;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveMeta;
import io.github.jwdeveloper.tiktok.live.TikTokLiveMeta;
import io.github.jwdeveloper.tiktok.websocket.TikTokWebsocketClient;

import java.util.logging.Logger;

public class TikTokLiveClient implements LiveClient {
    private final TikTokLiveMeta meta;
    private final TikTokGiftManager giftManager;
    private final TikTokApiService apiClient;
    private final TikTokWebsocketClient webSocketClient;
    private final Logger logger;


    public TikTokLiveClient(TikTokLiveMeta tikTokLiveMeta,
                            TikTokApiService tikTokApiService,
                            TikTokWebsocketClient webSocketClient,
                            TikTokGiftManager tikTokGiftManager,
                            Logger logger) {
        this.meta = tikTokLiveMeta;
        this.giftManager = tikTokGiftManager;
        this.apiClient = tikTokApiService;
        this.webSocketClient = webSocketClient;
        this.logger = logger;
    }


    public void run() {
        tryConnect();
    }

    public void stop() {
        if (!meta.hasConnectionState(ConnectionState.CONNECTED)) {
            return;
        }
        disconnect();
        setState(ConnectionState.DISCONNECTED);
    }

    public void tryConnect() {
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
            setState(ConnectionState.DISCONNECTED);
        }
    }

    public void connect() {
        if (meta.hasConnectionState(ConnectionState.CONNECTED))
            throw new RuntimeException("Already connected");
        if (meta.hasConnectionState(ConnectionState.CONNECTING))
            throw new RuntimeException("Already connecting");

        logger.info("Connecting");
        setState(ConnectionState.CONNECTING);

        var roomId = apiClient.fetchRoomId(meta.getUserName());
        meta.setRoomId(roomId);
        var roomData =apiClient.fetchRoomInfo();
        if (roomData.getStatus() == 0 || roomData.getStatus() == 4)
        {
            throw new TikTokLiveException("LiveStream for HostID could not be found. Is the Host online?");
        }

       // giftManager.loadGifts();
        var clientData = apiClient.fetchClientData();
        webSocketClient.start(clientData);
        setState(ConnectionState.CONNECTED);
    }

    public void disconnect() {

    }


    public LiveMeta getMeta() {
        return meta;
    }


    private void setState(ConnectionState connectionState) {
        logger.info("TikTokLive client state: " + connectionState.name());
        meta.setConnectionState(connectionState);
    }


}
