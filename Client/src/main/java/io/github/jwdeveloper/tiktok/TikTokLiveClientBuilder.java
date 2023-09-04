package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.TikTokEventBuilder;
import io.github.jwdeveloper.tiktok.events.TikTokEventConsumer;
import io.github.jwdeveloper.tiktok.events.messages.*;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.handlers.TikTokMessageHandlerRegistration;
import io.github.jwdeveloper.tiktok.http.TikTokApiService;
import io.github.jwdeveloper.tiktok.http.TikTokCookieJar;
import io.github.jwdeveloper.tiktok.http.TikTokHttpApiClient;
import io.github.jwdeveloper.tiktok.http.TikTokHttpRequestFactory;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.utils.CancelationToken;
import io.github.jwdeveloper.tiktok.websocket.TikTokWebSocketClient;

import java.time.Duration;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TikTokLiveClientBuilder implements TikTokEventBuilder<TikTokLiveClientBuilder> {
    private final ClientSettings clientSettings;
    private final Logger logger;
    private final TikTokEventHandler tikTokEventHandler;

    public TikTokLiveClientBuilder(String userName) {
        this.tikTokEventHandler = new TikTokEventHandler();
        this.clientSettings = Constants.DefaultClientSettings();
        this.clientSettings.setHostName(userName);
        this.logger = Logger.getLogger(TikTokLive.class.getName());
    }

    public TikTokLiveClientBuilder configure(Consumer<ClientSettings> consumer) {
        consumer.accept(clientSettings);
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

        if(clientSettings.isPrintToConsole() && clientSettings.getLogLevel() == Level.OFF)
        {
            logger.setLevel(Level.ALL);
        }
    }

    public LiveClient build() {
        validate();


        var tiktokRoomInfo = new TikTokRoomInfo();
        tiktokRoomInfo.setUserName(clientSettings.getHostName());

        var cookieJar = new TikTokCookieJar();
        var requestFactory = new TikTokHttpRequestFactory(cookieJar);
        var apiClient = new TikTokHttpApiClient(cookieJar, requestFactory);
        var apiService = new TikTokApiService(apiClient, logger, clientSettings);
        var giftManager = new TikTokGiftManager();
        var webResponseHandler = new TikTokMessageHandlerRegistration(tikTokEventHandler, clientSettings, logger, giftManager, tiktokRoomInfo);
        var webSocketClient = new TikTokWebSocketClient(logger,
                cookieJar,
                clientSettings,
                webResponseHandler,
                tikTokEventHandler);

        return new TikTokLiveClient(tiktokRoomInfo, apiService, webSocketClient, giftManager, tikTokEventHandler, clientSettings, logger);
    }

    public LiveClient buildAndRun() {
        var client = build();
        client.connect();
        return client;
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

    public TikTokLiveClientBuilder onShopMessage(TikTokEventConsumer<TikTokShopMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokShopMessageEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onDetectMessage(
            TikTokEventConsumer<TikTokDetectMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokDetectMessageEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onLinkLayerMessage(
            TikTokEventConsumer<TikTokLinkLayerMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkLayerMessageEvent.class, event);
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

    public TikTokLiveClientBuilder onRoomPinMessage(
            TikTokEventConsumer<TikTokRoomPinMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokRoomPinMessageEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onRoomMessage(TikTokEventConsumer<TikTokRoomMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokRoomMessageEvent.class, event);
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

    public TikTokLiveClientBuilder onLinkMessage(TikTokEventConsumer<TikTokLinkMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokLinkMessageEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onBarrageMessage(
            TikTokEventConsumer<TikTokBarrageMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokBarrageMessageEvent.class, event);
        return this;
    }

    public TikTokLiveClientBuilder onGiftMessage(TikTokEventConsumer<TikTokGiftMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokGiftMessageEvent.class, event);
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

    public TikTokLiveClientBuilder onPollMessage(TikTokEventConsumer<TikTokPollMessageEvent> event) {
        tikTokEventHandler.subscribe(TikTokPollMessageEvent.class, event);
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

    public TikTokLiveClientBuilder onUnhandled(TikTokEventConsumer<TikTokUnhandledEvent> event) {
        tikTokEventHandler.subscribe(TikTokUnhandledEvent.class, event);
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

    public TikTokLiveClientBuilder onGiftBroadcast(
            TikTokEventConsumer<TikTokGiftBroadcastEvent> event) {
        tikTokEventHandler.subscribe(TikTokGiftBroadcastEvent.class, event);
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
}







