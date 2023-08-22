package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.http.TikTokApiService;
import io.github.jwdeveloper.tiktok.live.ConnectionState;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import io.github.jwdeveloper.tiktok.live.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.websocket.TikTokWebSocketClient;

import java.util.logging.Logger;

public class TikTokLiveClient implements LiveClient {
    private final TikTokRoomInfo meta;
    private final TikTokGiftManager giftManager;
    private final TikTokApiService apiClient;
    private final TikTokWebSocketClient webSocketClient;
    private final TikTokEventHandler tikTokEventHandler;
    private final Logger logger;


    public TikTokLiveClient(TikTokRoomInfo tikTokLiveMeta,
                            TikTokApiService tikTokApiService,
                            TikTokWebSocketClient webSocketClient,
                            TikTokGiftManager tikTokGiftManager,
                            TikTokEventHandler tikTokEventHandler,
                            Logger logger) {
        this.meta = tikTokLiveMeta;
        this.giftManager = tikTokGiftManager;
        this.apiClient = tikTokApiService;
        this.webSocketClient = webSocketClient;
        this.logger = logger;
        this.tikTokEventHandler = tikTokEventHandler;
    }




    public void connect() {
        try {
            tryConnect();
        } catch (Exception e) {
            e.printStackTrace();
            setState(ConnectionState.DISCONNECTED);
        }
    }

    public void disconnect() {
        if (!meta.hasConnectionState(ConnectionState.CONNECTED)) {
            return;
        }
        webSocketClient.stop();
        setState(ConnectionState.DISCONNECTED);
    }


    public void tryConnect() {
        if (meta.hasConnectionState(ConnectionState.CONNECTED))
            throw new TikTokLiveException("Already connected");
        if (meta.hasConnectionState(ConnectionState.CONNECTING))
            throw new TikTokLiveException("Already connecting");

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




    public LiveRoomInfo getRoomInfo() {
        return meta;
    }


    private void setState(ConnectionState connectionState) {
        logger.info("TikTokLive client state: " + connectionState.name());
        meta.setConnectionState(connectionState);
    }


}
