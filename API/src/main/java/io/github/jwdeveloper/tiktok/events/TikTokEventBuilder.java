package io.github.jwdeveloper.tiktok.events;

import io.github.jwdeveloper.tiktok.events.messages.*;


public interface TikTokEventBuilder<T> {
    T onUnhandledSocial(TikTokEventConsumer<TikTokUnhandledSocialEvent> event);

    T onLinkMicFanTicket(TikTokEventConsumer<TikTokLinkMicFanTicketEvent> event);

    T onEnvelope(TikTokEventConsumer<TikTokEnvelopeEvent> event);

    T onShopMessage(TikTokEventConsumer<TikTokShopMessageEvent> event);

    T onDetectMessage(TikTokEventConsumer<TikTokDetectMessageEvent> event);

    T onLinkLayerMessage(TikTokEventConsumer<TikTokLinkLayerMessageEvent> event);

    T onConnected(TikTokEventConsumer<TikTokConnectedEvent> event);

    T onCaption(TikTokEventConsumer<TikTokCaptionEvent> event);

    T onQuestion(TikTokEventConsumer<TikTokQuestionEvent> event);

    T onRoomPinMessage(TikTokEventConsumer<TikTokRoomPinMessageEvent> event);

    T onRoomMessage(TikTokEventConsumer<TikTokRoomMessageEvent> event);

    T onLivePaused(TikTokEventConsumer<TikTokLivePausedEvent> event);

    T onLike(TikTokEventConsumer<TikTokLikeEvent> event);

    T onLinkMessage(TikTokEventConsumer<TikTokLinkMessageEvent> event);

    T onBarrageMessage(TikTokEventConsumer<TikTokBarrageMessageEvent> event);

    T onGiftMessage(TikTokEventConsumer<TikTokGiftMessageEvent> event);

    T onLinkMicArmies(TikTokEventConsumer<TikTokLinkMicArmiesEvent> event);

    T onEmote(TikTokEventConsumer<TikTokEmoteEvent> event);

    T onUnauthorizedMember(TikTokEventConsumer<TikTokUnauthorizedMemberEvent> event);

    T onInRoomBanner(TikTokEventConsumer<TikTokInRoomBannerEvent> event);

    T onLinkMicMethod(TikTokEventConsumer<TikTokLinkMicMethodEvent> event);

    T onSubscribe(TikTokEventConsumer<TikTokSubscribeEvent> event);

    T onPollMessage(TikTokEventConsumer<TikTokPollMessageEvent> event);

    T onFollow(TikTokEventConsumer<TikTokFollowEvent> event);

    T onRoomViewerData(TikTokEventConsumer<TikTokRoomViewerDataEvent> event);

    T onGoalUpdate(TikTokEventConsumer<TikTokGoalUpdateEvent> event);

    T onComment(TikTokEventConsumer<TikTokCommentEvent> event);

    T onRankUpdate(TikTokEventConsumer<TikTokRankUpdateEvent> event);

    T onIMDelete(TikTokEventConsumer<TikTokIMDeleteEvent> event);

    T onLiveEnded(TikTokEventConsumer<TikTokLiveEndedEvent> event);

    T onError(TikTokEventConsumer<TikTokErrorEvent> event);

    T onUnhandled(TikTokEventConsumer<TikTokUnhandledEvent> event);

    T onJoin(TikTokEventConsumer<TikTokJoinEvent> event);

    T onRankText(TikTokEventConsumer<TikTokRankTextEvent> event);

    T onShare(TikTokEventConsumer<TikTokShareEvent> event);

    T onUnhandledMember(TikTokEventConsumer<TikTokUnhandledMemberEvent> event);

    T onSubNotify(TikTokEventConsumer<TikTokSubNotifyEvent> event);

    T onLinkMicBattle(TikTokEventConsumer<TikTokLinkMicBattleEvent> event);

    T onDisconnected(TikTokEventConsumer<TikTokDisconnectedEvent> event);

    T onGiftBroadcast(TikTokEventConsumer<TikTokGiftBroadcastEvent> event);

    T onUnhandledControl(TikTokEventConsumer<TikTokUnhandledControlEvent> event);

    T onEvent(TikTokEventConsumer<TikTokEvent> event);

    T onWebsocketMessage(TikTokEventConsumer<TikTokWebsocketMessageEvent> event);

}


