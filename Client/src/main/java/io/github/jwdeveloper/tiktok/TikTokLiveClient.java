package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.events.messages.TikTokDisconnectedEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveOfflineHostException;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.http.TikTokApiService;
import io.github.jwdeveloper.tiktok.live.ConnectionState;
import io.github.jwdeveloper.tiktok.live.GiftManager;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import io.github.jwdeveloper.tiktok.websocket.TikTokWebSocketClient;

import java.util.logging.Logger;

public class TikTokLiveClient implements LiveClient {
    private final TikTokRoomInfo liveRoomInfo;
    private final TikTokGiftManager tikTokGiftManager;
    private final TikTokApiService apiClient;
    private final TikTokWebSocketClient webSocketClient;
    private final TikTokEventHandler tikTokEventHandler;
    private final ClientSettings clientSettings;
    private final Logger logger;

    public TikTokLiveClient(TikTokRoomInfo tikTokLiveMeta,
                            TikTokApiService tikTokApiService,
                            TikTokWebSocketClient webSocketClient,
                            TikTokGiftManager tikTokGiftManager,
                            TikTokEventHandler tikTokEventHandler,
                            ClientSettings clientSettings,
                            Logger logger) {
        this.liveRoomInfo = tikTokLiveMeta;
        this.tikTokGiftManager = tikTokGiftManager;
        this.apiClient = tikTokApiService;
        this.webSocketClient = webSocketClient;
        this.tikTokEventHandler = tikTokEventHandler;
        this.clientSettings = clientSettings;
        this.logger = logger;
    }


    public void connect() {
        try {
            tryConnect();
        }
        catch (TikTokLiveException e)
        {
            setState(ConnectionState.DISCONNECTED);
            tikTokEventHandler.publish(this, new TikTokErrorEvent(e));
            tikTokEventHandler.publish(this, new TikTokDisconnectedEvent());

            if(e instanceof TikTokLiveOfflineHostException && clientSettings.isRetryOnConnectionFailure())
            {
                try {
                    Thread.sleep(clientSettings.getRetryConnectionTimeout().toMillis());
                }
                catch (Exception ignored){}
                logger.info("Reconnecting");
                this.connect();
            }
            throw e;
        }
    }

    public void disconnect() {
        if (!liveRoomInfo.hasConnectionState(ConnectionState.CONNECTED)) {
            return;
        }
        webSocketClient.stop();
        setState(ConnectionState.DISCONNECTED);
    }



    public void tryConnect() {
        if (liveRoomInfo.hasConnectionState(ConnectionState.CONNECTED))
            throw new TikTokLiveException("Already connected");
        if (liveRoomInfo.hasConnectionState(ConnectionState.CONNECTING))
            throw new TikTokLiveException("Already connecting");

        logger.info("Connecting");
        setState(ConnectionState.CONNECTING);

        var roomId = apiClient.fetchRoomId(liveRoomInfo.getUserName());
        liveRoomInfo.setRoomId(roomId);
        var roomData = apiClient.fetchRoomInfo();
        if (roomData.getStatus() == 0 || roomData.getStatus() == 4) {
            throw new TikTokLiveOfflineHostException("LiveStream for HostID could not be found. Is the Host online?");
        }

        if (clientSettings.isDownloadGiftInfo())
        {
            logger.info("Fetch Gift info");
            var gifts = apiClient.fetchAvailableGifts();
            tikTokGiftManager.loadGifsInfo(gifts);
        }
        var clientData = apiClient.fetchClientData();
        webSocketClient.start(clientData, this);
        setState(ConnectionState.CONNECTED);
    }


    public LiveRoomInfo getRoomInfo() {
        return liveRoomInfo;
    }

    @Override
    public GiftManager getGiftManager() {
        return tikTokGiftManager;
    }


    private void setState(ConnectionState connectionState) {
        logger.info("TikTokLive client state: " + connectionState.name());
        liveRoomInfo.setConnectionState(connectionState);
    }

    public void sendHeartbeat(){
        webSocketClient.sendHeartbeat();
    }

}
