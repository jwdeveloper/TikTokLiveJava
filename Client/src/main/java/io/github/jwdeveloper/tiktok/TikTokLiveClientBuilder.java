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

import io.github.jwdeveloper.tiktok.common.LoggerFactory;
import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.control.TikTokPreConnectionEvent;
import io.github.jwdeveloper.tiktok.data.events.envelop.TikTokChestEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.*;
import io.github.jwdeveloper.tiktok.data.events.http.TikTokHttpResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollEvent;
import io.github.jwdeveloper.tiktok.data.events.room.*;
import io.github.jwdeveloper.tiktok.data.events.social.*;
import io.github.jwdeveloper.tiktok.data.events.websocket.*;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.gifts.TikTokGiftsManager;
import io.github.jwdeveloper.tiktok.http.HttpClientFactory;
import io.github.jwdeveloper.tiktok.listener.*;
import io.github.jwdeveloper.tiktok.live.*;
import io.github.jwdeveloper.tiktok.live.builder.*;
import io.github.jwdeveloper.tiktok.mappers.*;
import io.github.jwdeveloper.tiktok.mappers.data.MappingResult;
import io.github.jwdeveloper.tiktok.mappers.handlers.*;
import io.github.jwdeveloper.tiktok.messages.webcast.*;
import io.github.jwdeveloper.tiktok.websocket.TikTokWebSocketClient;
import io.github.jwdeveloper.tiktok.websocket.TikTokWebSocketOfflineClient;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class TikTokLiveClientBuilder implements LiveClientBuilder {

    protected final LiveClientSettings clientSettings;
    protected final TikTokLiveEventHandler eventHandler;
    protected final List<TikTokEventListener> listeners;
    protected Consumer<TikTokMapper> onCustomMappings;
    protected Logger logger;
    protected GiftsManager giftsManager;

    public TikTokLiveClientBuilder(String userName) {
        this.clientSettings = LiveClientSettings.createDefault();
        this.clientSettings.setHostName(userName);
        this.eventHandler = new TikTokLiveEventHandler();
        this.listeners = new ArrayList<>();
        this.onCustomMappings = (e) -> {
        };
    }

    public LiveClientBuilder onMapping(Consumer<TikTokMapper> onCustomMappings) {
        this.onCustomMappings = onCustomMappings;
        return this;
    }

    public TikTokLiveClientBuilder configure(Consumer<LiveClientSettings> onConfigure) {
        onConfigure.accept(clientSettings);
        return this;
    }

    public TikTokLiveClientBuilder addListener(TikTokEventListener listener) {
        if (listener != null)
            listeners.add(listener);
        return this;
    }

    protected void validate() {
        if (clientSettings.getClientLanguage() == null || clientSettings.getClientLanguage().isEmpty())
            clientSettings.setClientLanguage("en");

        if (clientSettings.getHostName() == null || clientSettings.getHostName().isEmpty())
            throw new TikTokLiveException("HostName can not be null");

        if (clientSettings.getHostName().startsWith("@"))
            clientSettings.setHostName(clientSettings.getHostName().substring(1));

        if (clientSettings.getPingInterval() < 250)
            throw new TikTokLiveException("Minimum allowed ping interval is 250 millseconds");

        var httpSettings = clientSettings.getHttpSettings();
        httpSettings.getParams().put("app_language", clientSettings.getClientLanguage());
        httpSettings.getParams().put("webcast_language", clientSettings.getClientLanguage());

        this.logger = LoggerFactory.create(clientSettings.getHostName(), clientSettings);
        this.giftsManager = clientSettings.isFetchGifts() ? TikTokLive.gifts() : new TikTokGiftsManager(List.of());
    }

    public LiveClient build() {
        validate();

        var tiktokRoomInfo = new TikTokRoomInfo();
        tiktokRoomInfo.setHostName(clientSettings.getHostName());

        var listenerManager = new TikTokListenersManager(listeners, eventHandler);

        var httpClientFactory = new HttpClientFactory(clientSettings);

        var liveHttpClient = clientSettings.isOffline() ?
                new TikTokLiveHttpOfflineClient() :
                new TikTokLiveHttpClient(httpClientFactory, clientSettings);

        var eventsMapper = createMapper(giftsManager, tiktokRoomInfo);
        var messageHandler = new TikTokLiveMessageHandler(eventHandler, eventsMapper);

        var webSocketClient = clientSettings.isOffline() ?
                new TikTokWebSocketOfflineClient(eventHandler) :
                new TikTokWebSocketClient(
                        clientSettings,
                        messageHandler,
                        eventHandler);

        return new TikTokLiveClient(
                messageHandler,
                giftsManager,
                tiktokRoomInfo,
                liveHttpClient,
                webSocketClient,
                eventHandler,
                clientSettings,
                listenerManager,
                logger);
    }

    public TikTokLiveMapper createMapper(GiftsManager giftsManager, TikTokRoomInfo roomInfo) {


        var eventMapper = new TikTokGenericEventMapper();
        var mapper = new TikTokLiveMapper(new TikTokLiveMapperHelper(eventMapper));

        //ConnectionEvents events
        var commonHandler = new TikTokCommonEventHandler();
        var giftHandler = new TikTokGiftEventHandler(giftsManager, roomInfo);
        var roomInfoHandler = new TikTokRoomInfoEventHandler(roomInfo);
        var socialHandler = new TikTokSocialMediaEventHandler(roomInfo);


        mapper.forMessage(WebcastControlMessage.class, commonHandler::handleWebcastControlMessage);

        //Room status events
        mapper.forMessage(WebcastLiveIntroMessage.class, roomInfoHandler::handleIntro);
        mapper.forMessage(WebcastRoomUserSeqMessage.class, roomInfoHandler::handleUserRanking);
        mapper.forMessage(WebcastCaptionMessage.class, (inputBytes, messageName, mapperHelper) ->
        {
            var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, WebcastCaptionMessage.class);
            return MappingResult.of(messageObject, new TikTokCaptionEvent(messageObject));
        });


        //User Interactions events
        mapper.forMessage(WebcastChatMessage.class, (inputBytes, messageName, mapperHelper) ->
        {
            var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, WebcastChatMessage.class);
            return MappingResult.of(messageObject, new TikTokCommentEvent(messageObject));
        });
        mapper.forMessage(WebcastSubNotifyMessage.class, (inputBytes, messageName, mapperHelper) ->
        {
            var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, WebcastSubNotifyMessage.class);
            return MappingResult.of(messageObject, new TikTokSubscribeEvent(messageObject));
        });
        mapper.forMessage(WebcastEmoteChatMessage.class, (inputBytes, messageName, mapperHelper) ->
        {
            var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, WebcastEmoteChatMessage.class);
            return MappingResult.of(messageObject, new TikTokEmoteEvent(messageObject));
        });
        mapper.forMessage(WebcastQuestionNewMessage.class, (inputBytes, messageName, mapperHelper) ->
        {
            var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, WebcastQuestionNewMessage.class);
            return MappingResult.of(messageObject, new TikTokQuestionEvent(messageObject));
        });

        mapper.forMessage(WebcastLikeMessage.class, roomInfoHandler::handleLike);
        mapper.forMessage(WebcastGiftMessage.class, giftHandler::handleGifts);
        mapper.forMessage(WebcastSocialMessage.class, socialHandler::handle);
        mapper.forMessage(WebcastMemberMessage.class, roomInfoHandler::handleMemberMessage);


        //Host Interaction events
        mapper.forMessage(WebcastPollMessage.class, commonHandler::handlePollEvent);
        mapper.forMessage(WebcastRoomPinMessage.class, commonHandler::handlePinMessage);
        mapper.forMessage(WebcastChatMessage.class, (inputBytes, messageName, mapperHelper) ->
        {
            var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, WebcastChatMessage.class);
            return MappingResult.of(messageObject, new TikTokCommentEvent(messageObject));
        });


        //LinkMic events
        mapper.forMessage(WebcastLinkMicBattle.class, (inputBytes, messageName, mapperHelper) -> {
            var message = mapperHelper.bytesToWebcastObject(inputBytes, WebcastLinkMicBattle.class);
            return MappingResult.of(message, new TikTokLinkMicBattleEvent(message));
        });
        mapper.forMessage(WebcastLinkMicArmies.class, (inputBytes, messageName, mapperHelper) -> {
            var message = mapperHelper.bytesToWebcastObject(inputBytes, WebcastLinkMicArmies.class);
            return MappingResult.of(message, new TikTokLinkMicArmiesEvent(message));
        });
        // mapper.webcastObjectToConstructor(WebcastLinkMicMethod.class, TikTokLinkMicMethodEvent.class);
        //  mapper.webcastObjectToConstructor(WebcastLinkMicFanTicketMethod.class, TikTokLinkMicFanTicketEvent.class);

        //Rank events
        //   mapper.webcastObjectToConstructor(WebcastRankTextMessage.class, TikTokRankTextEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastRankUpdateMessage.class, TikTokRankUpdateEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastHourlyRankMessage.class, TikTokRankUpdateEvent.class);

        //Others events
        //  mapper.webcastObjectToConstructor(WebcastInRoomBannerMessage.class, TikTokInRoomBannerEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastMsgDetectMessage.class, TikTokDetectEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastBarrageMessage.class, TikTokBarrageEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastUnauthorizedMemberMessage.class, TikTokUnauthorizedMemberEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastOecLiveShoppingMessage.class, TikTokShopEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastImDeleteMessage.class, TikTokIMDeleteEvent.class);
        //  mapper.bytesToEvents(WebcastEnvelopeMessage.class, commonHandler::handleEnvelop);


        onCustomMappings.accept(mapper);
        return mapper;
    }


    public LiveClient buildAndConnect() {
        var client = build();
        client.connect();
        return client;
    }

    public CompletableFuture<LiveClient> buildAndConnectAsync() {
        return build().connectAsync();
    }

    public TikTokLiveClientBuilder onUnhandledSocial(EventConsumer<TikTokUnhandledSocialEvent> event) {
        eventHandler.subscribe(TikTokUnhandledSocialEvent.class, event);
        return this;
    }

    public LiveClientBuilder onChest(EventConsumer<TikTokChestEvent> event) {
        eventHandler.subscribe(TikTokChestEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicFanTicket(EventConsumer<TikTokLinkMicFanTicketEvent> event) {
        eventHandler.subscribe(TikTokLinkMicFanTicketEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onEnvelope(EventConsumer<TikTokEnvelopeEvent> event) {
        eventHandler.subscribe(TikTokEnvelopeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onShop(EventConsumer<TikTokShopEvent> event) {
        eventHandler.subscribe(TikTokShopEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onDetect(EventConsumer<TikTokDetectEvent> event) {
        eventHandler.subscribe(TikTokDetectEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkLayer(EventConsumer<TikTokLinkLayerEvent> event) {
        eventHandler.subscribe(TikTokLinkLayerEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onConnected(EventConsumer<TikTokConnectedEvent> event) {
        eventHandler.subscribe(TikTokConnectedEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onPreConnection(EventConsumer<TikTokPreConnectionEvent> event) {
        eventHandler.subscribe(TikTokPreConnectionEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onCaption(EventConsumer<TikTokCaptionEvent> event) {
        eventHandler.subscribe(TikTokCaptionEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onQuestion(EventConsumer<TikTokQuestionEvent> event) {
        eventHandler.subscribe(TikTokQuestionEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRoomPin(EventConsumer<TikTokRoomPinEvent> event) {
        eventHandler.subscribe(TikTokRoomPinEvent.class, event);
        return this;
    }

    @Override
    public <E extends TikTokEvent> LiveClientBuilder onEvent(Class<E> eventClass, EventConsumer<E> event) {
        eventHandler.subscribe(eventClass, event);
        return this;
    }

    @Override
    public TikTokLiveClientBuilder onRoomInfo(EventConsumer<TikTokRoomInfoEvent> event) {
        eventHandler.subscribe(TikTokRoomInfoEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLivePaused(EventConsumer<TikTokLivePausedEvent> event) {
        eventHandler.subscribe(TikTokLivePausedEvent.class, event);
        return this;
    }

    @Override
    public TikTokLiveClientBuilder onLiveUnpaused(EventConsumer<TikTokLiveUnpausedEvent> event) {
        eventHandler.subscribe(TikTokLiveUnpausedEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLike(EventConsumer<TikTokLikeEvent> event) {
        eventHandler.subscribe(TikTokLikeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLink(EventConsumer<TikTokLinkEvent> event) {
        eventHandler.subscribe(TikTokLinkEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onBarrage(EventConsumer<TikTokBarrageEvent> event) {
        eventHandler.subscribe(TikTokBarrageEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onGift(EventConsumer<TikTokGiftEvent> event) {
        eventHandler.subscribe(TikTokGiftEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onGiftCombo(EventConsumer<TikTokGiftComboEvent> event) {
        eventHandler.subscribe(TikTokGiftComboEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicArmies(EventConsumer<TikTokLinkMicArmiesEvent> event) {
        eventHandler.subscribe(TikTokLinkMicArmiesEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onEmote(EventConsumer<TikTokEmoteEvent> event) {
        eventHandler.subscribe(TikTokEmoteEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onUnauthorizedMember(EventConsumer<TikTokUnauthorizedMemberEvent> event) {
        eventHandler.subscribe(TikTokUnauthorizedMemberEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onInRoomBanner(EventConsumer<TikTokInRoomBannerEvent> event) {
        eventHandler.subscribe(TikTokInRoomBannerEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicMethod(EventConsumer<TikTokLinkMicMethodEvent> event) {
        eventHandler.subscribe(TikTokLinkMicMethodEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onSubscribe(EventConsumer<TikTokSubscribeEvent> event) {
        eventHandler.subscribe(TikTokSubscribeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onPoll(EventConsumer<TikTokPollEvent> event) {
        eventHandler.subscribe(TikTokPollEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onFollow(EventConsumer<TikTokFollowEvent> event) {
        eventHandler.subscribe(TikTokFollowEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onComment(EventConsumer<TikTokCommentEvent> event) {
        eventHandler.subscribe(TikTokCommentEvent.class, event);
        return this;
    }

    @Override
    public LiveClientBuilder onHttpResponse(EventConsumer<TikTokHttpResponseEvent> action) {
        eventHandler.subscribe(TikTokHttpResponseEvent.class, action);
        return this;
    }

    public TikTokLiveClientBuilder onGoalUpdate(EventConsumer<TikTokGoalUpdateEvent> event) {
        eventHandler.subscribe(TikTokGoalUpdateEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRankUpdate(EventConsumer<TikTokRankUpdateEvent> event) {
        eventHandler.subscribe(TikTokRankUpdateEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onIMDelete(EventConsumer<TikTokIMDeleteEvent> event) {
        eventHandler.subscribe(TikTokIMDeleteEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLiveEnded(EventConsumer<TikTokLiveEndedEvent> event) {
        eventHandler.subscribe(TikTokLiveEndedEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onError(EventConsumer<TikTokErrorEvent> event) {
        eventHandler.subscribe(TikTokErrorEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onJoin(EventConsumer<TikTokJoinEvent> event) {
        eventHandler.subscribe(TikTokJoinEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRankText(EventConsumer<TikTokRankTextEvent> event) {
        eventHandler.subscribe(TikTokRankTextEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onShare(EventConsumer<TikTokShareEvent> event) {
        eventHandler.subscribe(TikTokShareEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onUnhandledMember(EventConsumer<TikTokUnhandledMemberEvent> event) {
        eventHandler.subscribe(TikTokUnhandledMemberEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onSubNotify(EventConsumer<TikTokSubNotifyEvent> event) {
        eventHandler.subscribe(TikTokSubNotifyEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicBattle(EventConsumer<TikTokLinkMicBattleEvent> event) {
        eventHandler.subscribe(TikTokLinkMicBattleEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onDisconnected(EventConsumer<TikTokDisconnectedEvent> event) {
        eventHandler.subscribe(TikTokDisconnectedEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onUnhandledControl(EventConsumer<TikTokUnhandledControlEvent> event) {
        eventHandler.subscribe(TikTokUnhandledControlEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onEvent(EventConsumer<TikTokEvent> event) {
        eventHandler.subscribe(TikTokEvent.class, event);
        return this;
    }

    @Override
    public TikTokLiveClientBuilder onWebsocketResponse(EventConsumer<TikTokWebsocketResponseEvent> event) {
        eventHandler.subscribe(TikTokWebsocketResponseEvent.class, event);
        return this;
    }

    @Override
    public TikTokLiveClientBuilder onWebsocketMessage(EventConsumer<TikTokWebsocketMessageEvent> event) {
        eventHandler.subscribe(TikTokWebsocketMessageEvent.class, event);
        return this;
    }

    @Override
    public TikTokLiveClientBuilder onWebsocketUnhandledMessage(EventConsumer<TikTokWebsocketUnhandledMessageEvent> event) {
        eventHandler.subscribe(TikTokWebsocketUnhandledMessageEvent.class, event);
        return this;
    }

    @Override
    public TikTokLiveClientBuilder onReconnecting(EventConsumer<TikTokReconnectingEvent> event) {
        eventHandler.subscribe(TikTokReconnectingEvent.class, event);
        return this;
    }
}