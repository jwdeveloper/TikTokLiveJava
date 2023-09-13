package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.events.messages.TikTokDisconnectedEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokReconnectingEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveOfflineHostException;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.http.TikTokApiService;
import io.github.jwdeveloper.tiktok.listener.ListenersManager;
import io.github.jwdeveloper.tiktok.listener.TikTokListenersManager;
import io.github.jwdeveloper.tiktok.live.ConnectionState;
import io.github.jwdeveloper.tiktok.live.GiftManager;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import io.github.jwdeveloper.tiktok.websocket.TikTokWebSocketClient;

import java.util.logging.Logger;

public class TikTokLiveClient implements LiveClient {
    private final TikTokRoomInfo liveRoomInfo;
    private final TikTokGiftManager tikTokGiftManager;
    private final TikTokApiService apiService;
    private final TikTokWebSocketClient webSocketClient;
    private final TikTokEventObserver tikTokEventHandler;
    private final ClientSettings clientSettings;
    private final TikTokListenersManager listenersManager;
    private final Logger logger;

    public TikTokLiveClient(TikTokRoomInfo tikTokLiveMeta,
                            TikTokApiService tikTokApiService,
                            TikTokWebSocketClient webSocketClient,
                            TikTokGiftManager tikTokGiftManager,
                            TikTokEventObserver tikTokEventHandler,
                            ClientSettings clientSettings,
                            TikTokListenersManager listenersManager,
                            Logger logger) {
        this.liveRoomInfo = tikTokLiveMeta;
        this.tikTokGiftManager = tikTokGiftManager;
        this.apiService = tikTokApiService;
        this.webSocketClient = webSocketClient;
        this.tikTokEventHandler = tikTokEventHandler;
        this.clientSettings = clientSettings;
        this.listenersManager = listenersManager;
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
                tikTokEventHandler.publish(this, new TikTokReconnectingEvent());
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


        apiService.updateSessionId();
        var roomId = apiService.fetchRoomId(liveRoomInfo.getUserName());
        liveRoomInfo.setRoomId(roomId);
        var roomData = apiService.fetchRoomInfo();
        if (roomData.getStatus() == 0 || roomData.getStatus() == 4) {
            throw new TikTokLiveOfflineHostException("LiveStream for HostID could not be found. Is the Host online?");
        }

        if (clientSettings.isDownloadGiftInfo())
        {
            logger.info("Fetch Gift info");
            var gifts = apiService.fetchAvailableGifts();
            tikTokGiftManager.loadGifsInfo(gifts);
        }
        var clientData = apiService.fetchClientData();
        webSocketClient.start(clientData, this);
        setState(ConnectionState.CONNECTED);
    }


    public LiveRoomInfo getRoomInfo() {
        return liveRoomInfo;
    }
    @Override
    public ListenersManager getListenersManager()
    {
        return listenersManager;
    }

    @Override
    public GiftManager getGiftManager() {
        return tikTokGiftManager;
    }


    private void setState(ConnectionState connectionState) {
        logger.info("TikTokLive client state: " + connectionState.name());
        liveRoomInfo.setConnectionState(connectionState);
    }


}
