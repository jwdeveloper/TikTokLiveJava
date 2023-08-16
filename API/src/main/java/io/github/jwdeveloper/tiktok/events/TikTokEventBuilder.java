package io.github.jwdeveloper.tiktok.events;

import io.github.jwdeveloper.tiktok.events.messages.*;

import java.util.function.Consumer;



public interface TikTokEventBuilder<T> {
    T onUnhandledSocial(Consumer<TikTokUnhandledSocialEvent> event);

    T onLinkMicFanTicket(Consumer<TikTokLinkMicFanTicketEvent> event);

    T onEnvelope(Consumer<TikTokEnvelopeEvent> event);

    T onShopMessage(Consumer<TikTokShopMessageEvent> event);

    T onDetectMessage(Consumer<TikTokDetectMessageEvent> event);

    T onLinkLayerMessage(Consumer<TikTokLinkLayerMessageEvent> event);

    T onConnected(Consumer<TikTokConnectedEvent> event);

    T onCaption(Consumer<TikTokCaptionEvent> event);

    T onQuestion(Consumer<TikTokQuestionEvent> event);

    T onRoomPinMessage(Consumer<TikTokRoomPinMessageEvent> event);

    T onRoomMessage(Consumer<TikTokRoomMessageEvent> event);

    T onLivePaused(Consumer<TikTokLivePausedEvent> event);

    T onLike(Consumer<TikTokLikeEvent> event);

    T onLinkMessage(Consumer<TikTokLinkMessageEvent> event);

    T onBarrageMessage(Consumer<TikTokBarrageMessageEvent> event);

    T onGiftMessage(Consumer<TikTokGiftMessageEvent> event);

    T onLinkMicArmies(Consumer<TikTokLinkMicArmiesEvent> event);

    T onEmote(Consumer<TikTokEmoteEvent> event);

    T onUnauthorizedMember(Consumer<TikTokUnauthorizedMemberEvent> event);

    T onInRoomBanner(Consumer<TikTokInRoomBannerEvent> event);

    T onLinkMicMethod(Consumer<TikTokLinkMicMethodEvent> event);

    T onSubscribe(Consumer<TikTokSubscribeEvent> event);

    T onPollMessage(Consumer<TikTokPollMessageEvent> event);

    T onFollow(Consumer<TikTokFollowEvent> event);

    T onRoomViewerData(Consumer<TikTokRoomViewerDataEvent> event);

    T onGoalUpdate(Consumer<TikTokGoalUpdateEvent> event);

    T onComment(Consumer<TikTokCommentEvent> event);

    T onRankUpdate(Consumer<TikTokRankUpdateEvent> event);

    T onIMDelete(Consumer<TikTokIMDeleteEvent> event);

    T onLiveEnded(Consumer<TikTokLiveEndedEvent> event);

    T onError(Consumer<TikTokErrorEvent> event);

    T onUnhandled(Consumer<TikTokUnhandledEvent> event);

    T onJoin(Consumer<TikTokJoinEvent> event);

    T onRankText(Consumer<TikTokRankTextEvent> event);

    T onShare(Consumer<TikTokShareEvent> event);

    T onUnhandledMember(Consumer<TikTokUnhandledMemberEvent> event);

    T onSubNotify(Consumer<TikTokSubNotifyEvent> event);

    T onLinkMicBattle(Consumer<TikTokLinkMicBattleEvent> event);

    T onDisconnected(Consumer<TikTokDisconnectedEvent> event);

    T onGiftBroadcast(Consumer<TikTokGiftBroadcastEvent> event);

    T onUnhandledControl(Consumer<TikTokUnhandledControlEvent> event);

    T onEvent(Consumer<TikTokEvent> event);
}


