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

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.TikTokEventBuilder;
import io.github.jwdeveloper.tiktok.events.TikTokEventConsumer;
import io.github.jwdeveloper.tiktok.events.messages.*;
import io.github.jwdeveloper.tiktok.events.messages.TikTokConnectedEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokDisconnectedEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokGiftComboFinishedEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokBarrageEvent;
import io.github.jwdeveloper.tiktok.events.messages.poll.TikTokPollEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.gifts.TikTokGiftManager;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.handlers.TikTokMessageHandlerRegistration;
import io.github.jwdeveloper.tiktok.http.TikTokApiService;
import io.github.jwdeveloper.tiktok.http.TikTokCookieJar;
import io.github.jwdeveloper.tiktok.http.TikTokHttpClient;
import io.github.jwdeveloper.tiktok.http.TikTokHttpRequestFactory;
import io.github.jwdeveloper.tiktok.listener.TikTokEventListener;
import io.github.jwdeveloper.tiktok.listener.TikTokListenersManager;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.websocket.TikTokWebSocketClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TikTokLiveClientBuilder implements TikTokEventBuilder<TikTokLiveClientBuilder> {
    private final ClientSettings clientSettings;
    private final Logger logger;
    private final TikTokEventObserver tikTokEventHandler;
    private final List<TikTokEventListener> listeners;

    public TikTokLiveClientBuilder(String userName) {
        this.tikTokEventHandler = new TikTokEventObserver();
        this.clientSettings = Constants.DefaultClientSettings();
        this.clientSettings.setHostName(userName);
        this.logger = Logger.getLogger(TikTokLive.class.getName());
        this.listeners = new ArrayList<>();
    }

    public TikTokLiveClientBuilder configure(Consumer<ClientSettings> consumer) {
        consumer.accept(clientSettings);
        return this;
    }

    public TikTokLiveClientBuilder addListener(TikTokEventListener listener) {
        listeners.add(listener);
        return this;
    }

    private void validate() {

        if (clientSettings.getTimeout() == null) {
            clientSettings.setTimeout(Duration.ofSeconds(Constants.DEFAULT_TIMEOUT));
        }

        if (clientSettings.getClientLanguage() == null || clientSettings.getClientLanguage().equals("")) {
            clientSettings.setClientLanguage(Constants.DefaultClientSettings().getClientLanguage());
        }


        if (clientSettings.getHostName() == null || clientSettings.getHostName().equals("")) {
            throw new TikTokLiveException("HostName can not be null");
        }

        var params = clientSettings.getClientParameters();
        params.put("app_language", clientSettings.getClientLanguage());
        params.put("webcast_language", clientSettings.getClientLanguage());

        logger.setLevel(clientSettings.getLogLevel());

        if (clientSettings.isPrintToConsole() && clientSettings.getLogLevel() == Level.OFF) {
            logger.setLevel(Level.ALL);
        }
    }

    public LiveClient build() {
        validate();

        var tiktokRoomInfo = new TikTokRoomInfo();
        tiktokRoomInfo.setUserName(clientSettings.getHostName());

        var listenerManager = new TikTokListenersManager(listeners, tikTokEventHandler);
        var cookieJar = new TikTokCookieJar();
        var requestFactory = new TikTokHttpRequestFactory(cookieJar);
        var apiClient = new TikTokHttpClient(cookieJar, requestFactory);
        var apiService = new TikTokApiService(apiClient, logger, clientSettings);
        var giftManager = new TikTokGiftManager();

        var webResponseHandler = new TikTokMessageHandlerRegistration(tikTokEventHandler,
                giftManager,
                tiktokRoomInfo);

        var webSocketClient = new TikTokWebSocketClient(logger,
                cookieJar,
                clientSettings,
                webResponseHandler,
                tikTokEventHandler);

        return new TikTokLiveClient(tiktokRoomInfo,
                apiService,
                webSocketClient,
                giftManager,
                tikTokEventHandler,
                clientSettings,
                listenerManager,
                logger);
    }

    public LiveClient buildAndRun() {
        var client = build();
        client.connect();
        return client;
    }

    public Future<LiveClient> buildAndRunAsync() {
        var executor = Executors.newSingleThreadExecutor();
        var future = executor.submit(this::buildAndRun);
        executor.shutdown();
        return future;
    }

    public TikTokLiveClientBuilder onUnhandledSocial(
            TikTokEventConsumer<TikTokUnhandledSocialEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnhandledSocialEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicFanTicket(
            TikTokEventConsumer<TikTokLinkMicFanTicketEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMicFanTicketEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onEnvelope(TikTokEventConsumer<TikTokEnvelopeEvent> event) {
        tikTokEventHandler.subscribe(TikTokEnvelopeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onShop(TikTokEventConsumer<TikTokShopEvent> event) {
        tikTokEventHandler.subscribe(TikTokShopEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onDetect(
            TikTokEventConsumer<TikTokDetectEvent> event) {
        tikTokEventHandler.subscribe(TikTokDetectEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkLayer(
            TikTokEventConsumer<TikTokLinkLayerEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkLayerEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onConnected(TikTokEventConsumer<TikTokConnectedEvent> event) {
        tikTokEventHandler.subscribe(TikTokConnectedEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onCaption(TikTokEventConsumer<TikTokCaptionEvent> event) {
        tikTokEventHandler.subscribe(TikTokCaptionEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onQuestion(TikTokEventConsumer<TikTokQuestionEvent> event) {
        tikTokEventHandler.subscribe(TikTokQuestionEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRoomPin(
            TikTokEventConsumer<TikTokRoomPinEvent> event) {
        tikTokEventHandler.subscribe(TikTokRoomPinEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRoom(TikTokEventConsumer<TikTokRoomEvent> event) {
        tikTokEventHandler.subscribe(TikTokRoomEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLivePaused(TikTokEventConsumer<TikTokLivePausedEvent> event) {
        tikTokEventHandler.subscribe(TikTokLivePausedEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLike(TikTokEventConsumer<TikTokLikeEvent> event) {
        tikTokEventHandler.subscribe(TikTokLikeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLink(TikTokEventConsumer<TikTokLinkEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onBarrage(
            TikTokEventConsumer<TikTokBarrageEvent> event) {
        tikTokEventHandler.subscribe(TikTokBarrageEvent.class, event);
        return this;
    }


    public TikTokLiveClientBuilder onGift(TikTokEventConsumer<TikTokGiftEvent> event) {
        tikTokEventHandler.subscribe(TikTokGiftEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onGiftCombo(TikTokEventConsumer<TikTokGiftComboFinishedEvent> event) {
        tikTokEventHandler.subscribe(TikTokGiftComboFinishedEvent.class, event);
        return this;
    }


    public TikTokLiveClientBuilder onLinkMicArmies(
            TikTokEventConsumer<TikTokLinkMicArmiesEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMicArmiesEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onEmote(TikTokEventConsumer<TikTokEmoteEvent> event) {
        tikTokEventHandler.subscribe(TikTokEmoteEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onUnauthorizedMember(
            TikTokEventConsumer<TikTokUnauthorizedMemberEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnauthorizedMemberEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onInRoomBanner(
            TikTokEventConsumer<TikTokInRoomBannerEvent> event) {
        tikTokEventHandler.subscribe(TikTokInRoomBannerEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicMethod(
            TikTokEventConsumer<TikTokLinkMicMethodEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMicMethodEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onSubscribe(TikTokEventConsumer<TikTokSubscribeEvent> event) {
        tikTokEventHandler.subscribe(TikTokSubscribeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onPoll(TikTokEventConsumer<TikTokPollEvent> event) {
        tikTokEventHandler.subscribe(TikTokPollEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onFollow(TikTokEventConsumer<TikTokFollowEvent> event) {
        tikTokEventHandler.subscribe(TikTokFollowEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRoomViewerData(
            TikTokEventConsumer<TikTokRoomViewerDataEvent> event) {
        tikTokEventHandler.subscribe(TikTokRoomViewerDataEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onGoalUpdate(TikTokEventConsumer<TikTokGoalUpdateEvent> event) {
        tikTokEventHandler.subscribe(TikTokGoalUpdateEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onComment(TikTokEventConsumer<TikTokCommentEvent> event) {
        tikTokEventHandler.subscribe(TikTokCommentEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRankUpdate(TikTokEventConsumer<TikTokRankUpdateEvent> event) {
        tikTokEventHandler.subscribe(TikTokRankUpdateEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onIMDelete(TikTokEventConsumer<TikTokIMDeleteEvent> event) {
        tikTokEventHandler.subscribe(TikTokIMDeleteEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLiveEnded(TikTokEventConsumer<TikTokLiveEndedEvent> event) {
        tikTokEventHandler.subscribe(TikTokLiveEndedEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onError(TikTokEventConsumer<TikTokErrorEvent> event) {
        tikTokEventHandler.subscribe(TikTokErrorEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onUnhandled(TikTokEventConsumer<TikTokUnhandledWebsocketMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnhandledWebsocketMessageEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onJoin(TikTokEventConsumer<TikTokJoinEvent> event) {
        tikTokEventHandler.subscribe(TikTokJoinEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRankText(TikTokEventConsumer<TikTokRankTextEvent> event) {
        tikTokEventHandler.subscribe(TikTokRankTextEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onShare(TikTokEventConsumer<TikTokShareEvent> event) {
        tikTokEventHandler.subscribe(TikTokShareEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onUnhandledMember(
            TikTokEventConsumer<TikTokUnhandledMemberEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnhandledMemberEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onSubNotify(TikTokEventConsumer<TikTokSubNotifyEvent> event) {
        tikTokEventHandler.subscribe(TikTokSubNotifyEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicBattle(
            TikTokEventConsumer<TikTokLinkMicBattleEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMicBattleEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onDisconnected(
            TikTokEventConsumer<TikTokDisconnectedEvent> event) {
        tikTokEventHandler.subscribe(TikTokDisconnectedEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onUnhandledControl(
            TikTokEventConsumer<TikTokUnhandledControlEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnhandledControlEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onEvent(TikTokEventConsumer<TikTokEvent> event) {
        tikTokEventHandler.subscribe(TikTokEvent.class, event);
        return this;
    }

    @Override
    public TikTokLiveClientBuilder onWebsocketMessage(TikTokEventConsumer<TikTokWebsocketMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokWebsocketMessageEvent.class, event);
        return this;
    }

    @Override
    public TikTokLiveClientBuilder onReconnecting(TikTokEventConsumer<TikTokReconnectingEvent> event) {
        tikTokEventHandler.subscribe(TikTokReconnectingEvent.class, event);
        return this;
    }
}







