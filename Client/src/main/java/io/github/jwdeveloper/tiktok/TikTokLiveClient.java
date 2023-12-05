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

import io.github.jwdeveloper.tiktok.data.events.TikTokDisconnectedEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokReconnectingEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomInfoEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveOfflineHostException;
import io.github.jwdeveloper.tiktok.gifts.TikTokGiftManager;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.http.TikTokApiService;
import io.github.jwdeveloper.tiktok.listener.ListenersManager;
import io.github.jwdeveloper.tiktok.listener.TikTokListenersManager;
import io.github.jwdeveloper.tiktok.live.GiftManager;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import io.github.jwdeveloper.tiktok.live.LiveRoomMeta;
import io.github.jwdeveloper.tiktok.models.ConnectionState;
import io.github.jwdeveloper.tiktok.websocket.SocketClient;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class TikTokLiveClient implements LiveClient {
    private final TikTokRoomInfo liveRoomInfo;
    private final TikTokGiftManager tikTokGiftManager;
    private final TikTokApiService apiService;
    private final SocketClient webSocketClient;
    private final TikTokEventObserver tikTokEventHandler;
    private final ClientSettings clientSettings;
    private final TikTokListenersManager listenersManager;
    private final Logger logger;

    public TikTokLiveClient(TikTokRoomInfo tikTokLiveMeta,
                            TikTokApiService tikTokApiService,
                            SocketClient webSocketClient,
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


    public void connectAsync(Consumer<LiveClient> onConnection) {
        CompletableFuture.supplyAsync(() ->
        {
            connect();
            onConnection.accept(this);
            return this;
        });
    }

    public CompletableFuture<LiveClient> connectAsync() {
        return CompletableFuture.supplyAsync(() ->
        {
            connect();
            return this;
        });
    }

    public void connect() {
        try {
            tryConnect();
        } catch (TikTokLiveException e) {
            setState(ConnectionState.DISCONNECTED);
            tikTokEventHandler.publish(this, new TikTokErrorEvent(e));
            tikTokEventHandler.publish(this, new TikTokDisconnectedEvent());

            if (e instanceof TikTokLiveOfflineHostException && clientSettings.isRetryOnConnectionFailure()) {
                try {
                    Thread.sleep(clientSettings.getRetryConnectionTimeout().toMillis());
                } catch (Exception ignored) {
                }
                logger.info("Reconnecting");
                tikTokEventHandler.publish(this, new TikTokReconnectingEvent());
                this.connect();
            }
            throw e;
        } catch (Exception e) {
            logger.info("Unhandled exception report this bug to github https://github.com/jwdeveloper/TikTokLiveJava/issues");
            this.disconnect();
            e.printStackTrace();
        }
    }

    public void disconnect() {
        if (liveRoomInfo.hasConnectionState(ConnectionState.DISCONNECTED)) {
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

        setState(ConnectionState.CONNECTING);

        apiService.updateSessionId();

        if (clientSettings.getRoomId() != null) {
            liveRoomInfo.setRoomId(clientSettings.getRoomId());
            logger.info("Using roomID from settings: " + clientSettings.getRoomId());
        } else {
            var roomId = apiService.fetchRoomId(liveRoomInfo.getHostName());
            liveRoomInfo.setRoomId(roomId);
        }


        var liveRoomMeta = apiService.fetchRoomInfo();
        if (liveRoomMeta.getStatus() == LiveRoomMeta.LiveRoomStatus.HostNotFound) {
            throw new TikTokLiveOfflineHostException("LiveStream for Host name could not be found.");
        }
        if (liveRoomMeta.getStatus() == LiveRoomMeta.LiveRoomStatus.HostOffline) {
            throw new TikTokLiveOfflineHostException("LiveStream for not be found, is the Host offline?");
        }

        liveRoomInfo.setTitle(liveRoomMeta.getTitie());
        liveRoomInfo.setViewersCount(liveRoomMeta.getViewers());
        liveRoomInfo.setTotalViewersCount(liveRoomMeta.getTotalViewers());
        liveRoomInfo.setAgeRestricted(liveRoomMeta.isAgeRestricted());
        liveRoomInfo.setHost(liveRoomMeta.getHost());

        var clientData = apiService.fetchClientData();
        webSocketClient.start(clientData, this);
        setState(ConnectionState.CONNECTED);
        tikTokEventHandler.publish(this, new TikTokRoomInfoEvent(liveRoomInfo));
    }


    public LiveRoomInfo getRoomInfo() {
        return liveRoomInfo;
    }

    @Override
    public ListenersManager getListenersManager() {
        return listenersManager;
    }

    @Override
    public Logger getLogger() {
        return logger;
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
