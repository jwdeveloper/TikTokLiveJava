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

import io.github.jwdeveloper.dependance.Dependance;
import io.github.jwdeveloper.dependance.api.DependanceContainer;
import io.github.jwdeveloper.tiktok.mappers.MessagesMapperFactory;
import io.github.jwdeveloper.tiktok.common.LoggerFactory;
import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.control.TikTokPreConnectionEvent;
import io.github.jwdeveloper.tiktok.data.events.envelop.TikTokChestEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.*;
import io.github.jwdeveloper.tiktok.data.events.http.TikTokHttpResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.link.*;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollEvent;
import io.github.jwdeveloper.tiktok.data.events.room.*;
import io.github.jwdeveloper.tiktok.data.events.social.*;
import io.github.jwdeveloper.tiktok.data.events.websocket.*;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.gifts.TikTokGiftsManager;
import io.github.jwdeveloper.tiktok.http.HttpClientFactory;
import io.github.jwdeveloper.tiktok.http.LiveHttpClient;
import io.github.jwdeveloper.tiktok.listener.*;
import io.github.jwdeveloper.tiktok.live.*;
import io.github.jwdeveloper.tiktok.live.builder.*;
import io.github.jwdeveloper.tiktok.mappers.*;
import io.github.jwdeveloper.tiktok.mappers.handlers.TikTokCommonEventHandler;
import io.github.jwdeveloper.tiktok.mappers.handlers.TikTokGiftEventHandler;
import io.github.jwdeveloper.tiktok.mappers.handlers.TikTokRoomInfoEventHandler;
import io.github.jwdeveloper.tiktok.mappers.handlers.TikTokSocialMediaEventHandler;
import io.github.jwdeveloper.tiktok.websocket.*;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class TikTokLiveClientBuilder implements LiveClientBuilder {

    protected final LiveClientSettings clientSettings;
    protected final TikTokLiveEventHandler eventHandler;
    protected final List<TikTokEventListener> listeners;
    protected final List<Consumer<TikTokMapper>> onCustomMappings;

    public TikTokLiveClientBuilder(String userName) {
        this.clientSettings = LiveClientSettings.createDefault();
        this.clientSettings.setHostName(userName);
        this.eventHandler = new TikTokLiveEventHandler();
        this.listeners = new ArrayList<>();
        this.onCustomMappings = new ArrayList<>();
    }

    public LiveClientBuilder onMapping(Consumer<TikTokMapper> consumer) {
        this.onCustomMappings.add(consumer);
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
    }

    //TODO each class registered to container should implement own interface,
    public LiveClient build() {
        validate();

        //Docs: https://github.com/jwdeveloper/DepenDance?tab=readme-ov-file#01-basic
        var dependance = Dependance.newContainer();

        //config
        dependance.registerSingleton(LiveClientSettings.class, clientSettings);
        dependance.registerSingleton(TikTokRoomInfo.class, container ->
        {
            var roomInfo = new TikTokRoomInfo();
            roomInfo.setHostName(clientSettings.getHostName());
            return roomInfo;
        });
        dependance.registerSingleton(Logger.class, LoggerFactory.create(clientSettings.getHostName(), clientSettings));

        //messages
        dependance.registerSingleton(TikTokLiveEventHandler.class, eventHandler);
        dependance.registerSingleton(TikTokLiveMessageHandler.class);
        dependance.registerSingleton(TikTokLiveMapper.class, (container) ->
        {
            var dependace = (DependanceContainer) container.find(DependanceContainer.class);
            var mapper = MessagesMapperFactory.create(dependace);
            onCustomMappings.forEach(action -> action.accept(mapper));
            return mapper;
        });

        //listeners
        dependance.registerSingletonList(TikTokEventListener.class, (e) -> listeners);
        dependance.registerSingleton(ListenersManager.class, TikTokListenersManager.class);

        //networking
        dependance.registerSingleton(HttpClientFactory.class);
        if (clientSettings.isOffline()) {
            dependance.registerSingleton(SocketClient.class, TikTokWebSocketOfflineClient.class);
            dependance.registerSingleton(LiveHttpClient.class, TikTokLiveHttpOfflineClient.class);
        } else {
            dependance.registerSingleton(SocketClient.class, TikTokWebSocketClient.class);
            dependance.registerSingleton(LiveHttpClient.class, TikTokLiveHttpClient.class);
        }

        /** TODO in future, custom proxy implementation that can be provided via builder
         *  if(customProxy != null)
         *     dependance.registerSingleton(TikTokProxyProvider.class,customProxy);
         *  else
         *     dependance.registerSingleton(TikTokProxyProvider.class,DefaultProxyProvider.class);
         */

        //gifts
        if (clientSettings.isFetchGifts()) {
            dependance.registerSingleton(GiftsManager.class, TikTokLive.gifts());
        } else {
            dependance.registerSingleton(GiftsManager.class, new TikTokGiftsManager(List.of()));
        }

        //mapper
        dependance.registerSingleton(TikTokGenericEventMapper.class);
        dependance.registerSingleton(TikTokMapperHelper.class, TikTokLiveMapperHelper.class);
        dependance.registerSingleton(TikTokLiveMapper.class);

        //mapper handlers
        dependance.registerSingleton(TikTokCommonEventHandler.class);
        dependance.registerSingleton(TikTokGiftEventHandler.class);
        dependance.registerSingleton(TikTokRoomInfoEventHandler.class);
        dependance.registerSingleton(TikTokSocialMediaEventHandler.class);




        //client
        dependance.registerSingleton(LiveClient.class, TikTokLiveClient.class);

        var container = dependance.build();
        return container.find(LiveClient.class);
    }

    public LiveClient buildAndConnect() {
        var client = build();
        client.connect();
        return client;
    }

    public CompletableFuture<LiveClient> buildAndConnectAsync() {
        return build().connectAsync();
    }

    /**
     * To do figure out how to use Annotation processor can could dynamically
     * like Lombok generates methods for all possible events, everytime library
     * is compiled
     */
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

    public TikTokLiveClientBuilder onLinkInvite(EventConsumer<TikTokLinkInviteEvent> event) {
        eventHandler.subscribe(TikTokLinkInviteEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkReply(EventConsumer<TikTokLinkReplyEvent> event) {
        eventHandler.subscribe(TikTokLinkReplyEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkCreate(EventConsumer<TikTokLinkCreateEvent> event) {
        eventHandler.subscribe(TikTokLinkCreateEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkClose(EventConsumer<TikTokLinkCloseEvent> event) {
        eventHandler.subscribe(TikTokLinkCloseEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkEnter(EventConsumer<TikTokLinkEnterEvent> event) {
        eventHandler.subscribe(TikTokLinkEnterEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkLeave(EventConsumer<TikTokLinkLeaveEvent> event) {
        eventHandler.subscribe(TikTokLinkLeaveEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkCancel(EventConsumer<TikTokLinkCancelEvent> event) {
        eventHandler.subscribe(TikTokLinkCancelEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkKickOut(EventConsumer<TikTokLinkKickOutEvent> event) {
        eventHandler.subscribe(TikTokLinkKickOutEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkLinkedListChange(EventConsumer<TikTokLinkLinkedListChangeEvent> event) {
        eventHandler.subscribe(TikTokLinkLinkedListChangeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkUpdateUser(EventConsumer<TikTokLinkUpdateUserEvent> event) {
        eventHandler.subscribe(TikTokLinkUpdateUserEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkWaitListChange(EventConsumer<TikTokLinkWaitListChangeEvent> event) {
        eventHandler.subscribe(TikTokLinkWaitListChangeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMute(EventConsumer<TikTokLinkMuteEvent> event) {
        eventHandler.subscribe(TikTokLinkMuteEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkRandomMatch(EventConsumer<TikTokLinkRandomMatchEvent> event) {
        eventHandler.subscribe(TikTokLinkRandomMatchEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkUpdateUserSettings(EventConsumer<TikTokLinkUpdateUserSettingEvent> event) {
        eventHandler.subscribe(TikTokLinkUpdateUserSettingEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMicIdxUpdate(EventConsumer<TikTokLinkMicIdxUpdateEvent> event) {
        eventHandler.subscribe(TikTokLinkMicIdxUpdateEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkListChange(EventConsumer<TikTokLinkListChangeEvent> event) {
        eventHandler.subscribe(TikTokLinkListChangeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkCohostListChange(EventConsumer<TikTokLinkCohostListChangeEvent> event) {
        eventHandler.subscribe(TikTokLinkCohostListChangeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkMediaChange(EventConsumer<TikTokLinkMediaChangeEvent> event) {
        eventHandler.subscribe(TikTokLinkMediaChangeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkAcceptNotice(EventConsumer<TikTokLinkAcceptNoticeEvent> event) {
        eventHandler.subscribe(TikTokLinkAcceptNoticeEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkSysKickOut(EventConsumer<TikTokLinkSysKickOutEvent> event) {
        eventHandler.subscribe(TikTokLinkSysKickOutEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkUserToast(EventConsumer<TikTokLinkUserToastEvent> event) {
        eventHandler.subscribe(TikTokLinkUserToastEvent.class, event);
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