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

import com.google.protobuf.ByteString;
import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.control.*;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomInfoEvent;
import io.github.jwdeveloper.tiktok.data.requests.*;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.*;
import io.github.jwdeveloper.tiktok.http.LiveHttpClient;
import io.github.jwdeveloper.tiktok.listener.*;
import io.github.jwdeveloper.tiktok.live.*;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.models.ConnectionState;
import io.github.jwdeveloper.tiktok.websocket.LiveSocketClient;
import lombok.Getter;

import java.util.Base64;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

@Getter
public class TikTokLiveClient implements LiveClient
{
    private final TikTokRoomInfo roomInfo;
    private final LiveHttpClient httpClient;
    private final LiveSocketClient webSocketClient;
    private final LiveEventsHandler tikTokEventHandler;
    private final LiveClientSettings clientSettings;
    private final ListenersManager listenersManager;
    private final Logger logger;
    private final GiftsManager giftManager;
    private final LiveMessagesHandler messageHandler;

    public TikTokLiveClient(
            LiveMessagesHandler messageHandler,
            GiftsManager giftsManager,
            TikTokRoomInfo tikTokLiveMeta,
            LiveHttpClient tiktokHttpClient,
            LiveSocketClient webSocketClient,
            LiveEventsHandler tikTokEventHandler,
            LiveClientSettings clientSettings,
            ListenersManager listenersManager,
            Logger logger) {
        this.messageHandler = messageHandler;
        this.giftManager = giftsManager;
        this.roomInfo = tikTokLiveMeta;
        this.httpClient = tiktokHttpClient;
        this.webSocketClient = webSocketClient;
        this.tikTokEventHandler = tikTokEventHandler;
        this.clientSettings = clientSettings;
        this.listenersManager = listenersManager;
        this.logger = logger;
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
                } catch (Exception ignored) {}
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

    public void tryConnect() {
        if (!roomInfo.hasConnectionState(ConnectionState.DISCONNECTED)) {
            throw new TikTokLiveException("Already connected");
        }

        setState(ConnectionState.CONNECTING);
        tikTokEventHandler.publish(this, new TikTokConnectingEvent());
        var userDataRequest = new LiveUserData.Request(roomInfo.getHostName());
        var userData = httpClient.fetchLiveUserData(userDataRequest);
        roomInfo.setStartTime(userData.getStartTime());
        roomInfo.setRoomId(userData.getRoomId());

        if (userData.getUserStatus() == LiveUserData.UserStatus.Offline)
            throw new TikTokLiveOfflineHostException("User is offline: " + roomInfo.getHostName());

        if (userData.getUserStatus() == LiveUserData.UserStatus.NotFound)
            throw new TikTokLiveUnknownHostException("User not found: " + roomInfo.getHostName());

        var liveDataRequest = new LiveData.Request(userData.getRoomId());
        var liveData = httpClient.fetchLiveData(liveDataRequest);

        if (liveData.isAgeRestricted() && clientSettings.isThrowOnAgeRestriction())
            throw new TikTokLiveException("Livestream for " + roomInfo.getHostName() + " is 18+ or age restricted!");

        if (liveData.getLiveStatus() == LiveData.LiveStatus.HostNotFound)
            throw new TikTokLiveUnknownHostException("LiveStream for " + roomInfo.getHostName() + " could not be found.");

        if (liveData.getLiveStatus() == LiveData.LiveStatus.HostOffline)
            throw new TikTokLiveOfflineHostException("LiveStream for " + roomInfo.getHostName() + " not found, is the Host offline?");

        roomInfo.setTitle(liveData.getTitle());
        roomInfo.setViewersCount(liveData.getViewers());
        roomInfo.setTotalViewersCount(liveData.getTotalViewers());
        roomInfo.setAgeRestricted(liveData.isAgeRestricted());
        roomInfo.setHost(liveData.getHost());

        var preconnectEvent = new TikTokPreConnectionEvent(userData, liveData);
        tikTokEventHandler.publish(this, preconnectEvent);
        if (preconnectEvent.isCancelConnection())
            throw new TikTokLiveException("TikTokPreConnectionEvent cancelled connection!");

        if (clientSettings.isFetchGifts())
            giftManager.attachGiftsList(httpClient.fetchRoomGiftsData(userData.getRoomId()).getGifts());

        var liveConnectionRequest = new LiveConnectionData.Request(userData.getRoomId());
        var liveConnectionData = httpClient.fetchLiveConnectionData(liveConnectionRequest);
        webSocketClient.start(liveConnectionData, this);

        setState(ConnectionState.CONNECTED);
        tikTokEventHandler.publish(this, new TikTokRoomInfoEvent(roomInfo));
    }

    public void disconnect() {
        if (roomInfo.hasConnectionState(ConnectionState.DISCONNECTED)) {
            return;
        }
        setState(ConnectionState.DISCONNECTED);
        webSocketClient.stop();
    }

    private void setState(ConnectionState connectionState) {
        logger.info("TikTokLive client state: " + connectionState.name());
        roomInfo.setConnectionState(connectionState);
    }

    public void publishEvent(TikTokEvent event) {
        tikTokEventHandler.publish(this, event);
    }

    @Override
    public void publishMessage(String webcastMessageName, String payloadBase64) {
        this.publishMessage(webcastMessageName, Base64.getDecoder().decode(payloadBase64));
    }
    @Override
    public void publishMessage(String webcastMessageName, byte[] payload) {

        var builder = WebcastResponse.Message.newBuilder();
        builder.setMethod(webcastMessageName);
        builder.setPayload(ByteString.copyFrom(payload));
        var message = builder.build();
        messageHandler.handleSingleMessage(this, message);
    }


    public void connectAsync(Consumer<LiveClient> onConnection) {
        connectAsync().thenAccept(onConnection);
    }

    public CompletableFuture<LiveClient> connectAsync() {
        return CompletableFuture.supplyAsync(() -> {
            connect();
            return this;
        });
    }
}