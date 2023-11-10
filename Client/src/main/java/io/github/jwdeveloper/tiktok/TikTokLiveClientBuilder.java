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

import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.envelop.TikTokChestEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftComboEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomPinEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomUserInfoEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokShareEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketMessageEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketUnhandledMessageEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.gifts.TikTokGiftManager;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.handlers.TikTokMessageHandlerRegistration;
import io.github.jwdeveloper.tiktok.handlers.events.TikTokGiftEventHandler;
import io.github.jwdeveloper.tiktok.http.TikTokApiService;
import io.github.jwdeveloper.tiktok.http.TikTokCookieJar;
import io.github.jwdeveloper.tiktok.http.TikTokHttpClient;
import io.github.jwdeveloper.tiktok.http.TikTokHttpRequestFactory;
import io.github.jwdeveloper.tiktok.listener.TikTokEventListener;
import io.github.jwdeveloper.tiktok.listener.TikTokListenersManager;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.builder.EventConsumer;
import io.github.jwdeveloper.tiktok.live.builder.LiveClientBuilder;
import io.github.jwdeveloper.tiktok.mappers.TikTokGenericEventMapper;
import io.github.jwdeveloper.tiktok.utils.ConsoleColors;
import io.github.jwdeveloper.tiktok.websocket.TikTokWebSocketClient;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.*;

public class TikTokLiveClientBuilder implements LiveClientBuilder {

    protected final ClientSettings clientSettings;
    protected final Logger logger;
    protected final TikTokEventObserver tikTokEventHandler;
    protected final List<TikTokEventListener> listeners;

    public TikTokLiveClientBuilder(String userName) {
        this.tikTokEventHandler = new TikTokEventObserver();
        this.clientSettings = Constants.DefaultClientSettings();
        this.clientSettings.setHostName(userName);
        this.logger = Logger.getLogger(TikTokLive.class.getSimpleName() + " " + userName);
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

    protected void validate() {

        if (clientSettings.getTimeout() == null) {
            clientSettings.setTimeout(Duration.ofSeconds(Constants.DEFAULT_TIMEOUT));
        }

        if (clientSettings.getClientLanguage() == null || clientSettings.getClientLanguage().equals("")) {
            clientSettings.setClientLanguage(Constants.DefaultClientSettings().getClientLanguage());
        }


        if (clientSettings.getHostName() == null || clientSettings.getHostName().equals("")) {
            throw new TikTokLiveException("HostName can not be null");
        }

        if (clientSettings.getHostName().startsWith("@"))
        {
            clientSettings.setHostName(clientSettings.getHostName().substring(1));
        }


        var params = clientSettings.getClientParameters();
        params.put("app_language", clientSettings.getClientLanguage());
        params.put("webcast_language", clientSettings.getClientLanguage());


        var handler = new ConsoleHandler();
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                var sb = new StringBuilder();
                sb.append(ConsoleColors.GREEN).append("[").append(record.getLoggerName()).append("] ");
                sb.append(ConsoleColors.GREEN).append("[").append(record.getLevel()).append("]: ");
                sb.append(ConsoleColors.WHITE_BRIGHT).append(record.getMessage());
                sb.append(ConsoleColors.RESET).append("\n");
                return sb.toString();
            }
        });
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);

        logger.setLevel(clientSettings.getLogLevel());

        if (clientSettings.isPrintToConsole() && clientSettings.getLogLevel() == Level.OFF) {
            logger.setLevel(Level.ALL);
        }



    }

    public LiveClient build() {
        validate();

        var tiktokRoomInfo = new TikTokRoomInfo();
        tiktokRoomInfo.setHostName(clientSettings.getHostName());

        var listenerManager = new TikTokListenersManager(listeners, tikTokEventHandler);
        var cookieJar = new TikTokCookieJar();
        var requestFactory = new TikTokHttpRequestFactory(cookieJar);
        var apiClient = new TikTokHttpClient(cookieJar, requestFactory);
        var apiService = new TikTokApiService(apiClient, logger, clientSettings);
        var giftManager = new TikTokGiftManager(logger);
        var eventMapper = new TikTokGenericEventMapper();
        var giftHandler = new TikTokGiftEventHandler(giftManager);

        var webResponseHandler = new TikTokMessageHandlerRegistration(tikTokEventHandler,
                tiktokRoomInfo,
                eventMapper,
                giftHandler
                );

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

    public LiveClient buildAndConnect() {
        var client = build();
        client.connect();
        return client;
    }

    public CompletableFuture<LiveClient> buildAndConnectAsync() {
        return build().connectAsync();
    }

    public TikTokLiveClientBuilder onUnhandledSocial(
            EventConsumer<TikTokUnhandledSocialEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnhandledSocialEvent.class, event);
        return this;
    }

    @Override
    public LiveClientBuilder onChestOpen(EventConsumer<TikTokChestEvent> event) {
        tikTokEventHandler.subscribe(TikTokChestEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicFanTicket(
            EventConsumer<TikTokLinkMicFanTicketEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMicFanTicketEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onEnvelope(EventConsumer<TikTokEnvelopeEvent> event) {
        tikTokEventHandler.subscribe(TikTokEnvelopeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onShop(EventConsumer<TikTokShopEvent> event) {
        tikTokEventHandler.subscribe(TikTokShopEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onDetect(
            EventConsumer<TikTokDetectEvent> event) {
        tikTokEventHandler.subscribe(TikTokDetectEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkLayer(
            EventConsumer<TikTokLinkLayerEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkLayerEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onConnected(EventConsumer<TikTokConnectedEvent> event) {
        tikTokEventHandler.subscribe(TikTokConnectedEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onCaption(EventConsumer<TikTokCaptionEvent> event) {
        tikTokEventHandler.subscribe(TikTokCaptionEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onQuestion(EventConsumer<TikTokQuestionEvent> event) {
        tikTokEventHandler.subscribe(TikTokQuestionEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRoomPin(
            EventConsumer<TikTokRoomPinEvent> event) {
        tikTokEventHandler.subscribe(TikTokRoomPinEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRoom(EventConsumer<TikTokRoomEvent> event) {
        tikTokEventHandler.subscribe(TikTokRoomEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLivePaused(EventConsumer<TikTokLivePausedEvent> event) {
        tikTokEventHandler.subscribe(TikTokLivePausedEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLike(EventConsumer<TikTokLikeEvent> event) {
        tikTokEventHandler.subscribe(TikTokLikeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLink(EventConsumer<TikTokLinkEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onBarrage(
            EventConsumer<TikTokBarrageEvent> event) {
        tikTokEventHandler.subscribe(TikTokBarrageEvent.class, event);
        return this;
    }


    public TikTokLiveClientBuilder onGift(EventConsumer<TikTokGiftEvent> event) {
        tikTokEventHandler.subscribe(TikTokGiftEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onGiftCombo(EventConsumer<TikTokGiftComboEvent> event) {
        tikTokEventHandler.subscribe(TikTokGiftComboEvent.class, event);
        return this;
    }


    public TikTokLiveClientBuilder onLinkMicArmies(
            EventConsumer<TikTokLinkMicArmiesEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMicArmiesEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onEmote(EventConsumer<TikTokEmoteEvent> event) {
        tikTokEventHandler.subscribe(TikTokEmoteEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onUnauthorizedMember(
            EventConsumer<TikTokUnauthorizedMemberEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnauthorizedMemberEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onInRoomBanner(
            EventConsumer<TikTokInRoomBannerEvent> event) {
        tikTokEventHandler.subscribe(TikTokInRoomBannerEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicMethod(
            EventConsumer<TikTokLinkMicMethodEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMicMethodEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onSubscribe(EventConsumer<TikTokSubscribeEvent> event) {
        tikTokEventHandler.subscribe(TikTokSubscribeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onPoll(EventConsumer<TikTokPollEvent> event) {
        tikTokEventHandler.subscribe(TikTokPollEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onFollow(EventConsumer<TikTokFollowEvent> event) {
        tikTokEventHandler.subscribe(TikTokFollowEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRoomUserInfo(
            EventConsumer<TikTokRoomUserInfoEvent> event) {
        tikTokEventHandler.subscribe(TikTokRoomUserInfoEvent.class, event);
        return this;
    }


    public TikTokLiveClientBuilder onComment(EventConsumer<TikTokCommentEvent> event) {
        tikTokEventHandler.subscribe(TikTokCommentEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onGoalUpdate(EventConsumer<TikTokGoalUpdateEvent> event) {
        tikTokEventHandler.subscribe(TikTokGoalUpdateEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRankUpdate(EventConsumer<TikTokRankUpdateEvent> event) {
        tikTokEventHandler.subscribe(TikTokRankUpdateEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onIMDelete(EventConsumer<TikTokIMDeleteEvent> event) {
        tikTokEventHandler.subscribe(TikTokIMDeleteEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLiveEnded(EventConsumer<TikTokLiveEndedEvent> event) {
        tikTokEventHandler.subscribe(TikTokLiveEndedEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onError(EventConsumer<TikTokErrorEvent> event) {
        tikTokEventHandler.subscribe(TikTokErrorEvent.class, event);
        return this;
    }


    public TikTokLiveClientBuilder onJoin(EventConsumer<TikTokJoinEvent> event) {
        tikTokEventHandler.subscribe(TikTokJoinEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRankText(EventConsumer<TikTokRankTextEvent> event) {
        tikTokEventHandler.subscribe(TikTokRankTextEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onShare(EventConsumer<TikTokShareEvent> event) {
        tikTokEventHandler.subscribe(TikTokShareEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onUnhandledMember(
            EventConsumer<TikTokUnhandledMemberEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnhandledMemberEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onSubNotify(EventConsumer<TikTokSubNotifyEvent> event) {
        tikTokEventHandler.subscribe(TikTokSubNotifyEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicBattle(
            EventConsumer<TikTokLinkMicBattleEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMicBattleEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onDisconnected(
            EventConsumer<TikTokDisconnectedEvent> event) {
        tikTokEventHandler.subscribe(TikTokDisconnectedEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onUnhandledControl(
            EventConsumer<TikTokUnhandledControlEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnhandledControlEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onEvent(EventConsumer<TikTokEvent> event) {
        tikTokEventHandler.subscribe(TikTokEvent.class, event);
        return this;
    }

    @Override
    public TikTokLiveClientBuilder onWebsocketResponse(EventConsumer<TikTokWebsocketResponseEvent> event) {
        tikTokEventHandler.subscribe(TikTokWebsocketResponseEvent.class, event);
        return this;
    }

    @Override
    public TikTokLiveClientBuilder onWebsocketMessage(EventConsumer<TikTokWebsocketMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokWebsocketMessageEvent.class, event);
        return this;
    }

    @Override
    public TikTokLiveClientBuilder onWebsocketUnhandledMessage(EventConsumer<TikTokWebsocketUnhandledMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokWebsocketUnhandledMessageEvent.class, event);
        return this;
    }

    @Override
    public TikTokLiveClientBuilder onReconnecting(EventConsumer<TikTokReconnectingEvent> event) {
        tikTokEventHandler.subscribe(TikTokReconnectingEvent.class, event);
        return this;
    }
}







