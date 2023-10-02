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
package io.github.jwdeveloper.tiktok.events;

import io.github.jwdeveloper.tiktok.events.messages.*;


public interface TikTokEventBuilder<T> {
    T onUnhandledSocial(TikTokEventConsumer<TikTokUnhandledSocialEvent> event);

    T onLinkMicFanTicket(TikTokEventConsumer<TikTokLinkMicFanTicketEvent> event);

    T onEnvelope(TikTokEventConsumer<TikTokEnvelopeEvent> event);

    T onShop(TikTokEventConsumer<TikTokShopEvent> event);

    T onDetect(TikTokEventConsumer<TikTokDetectEvent> event);

    T onLinkLayer(TikTokEventConsumer<TikTokLinkLayerEvent> event);

    T onConnected(TikTokEventConsumer<TikTokConnectedEvent> event);

    T onCaption(TikTokEventConsumer<TikTokCaptionEvent> event);

    T onQuestion(TikTokEventConsumer<TikTokQuestionEvent> event);

    T onRoomPin(TikTokEventConsumer<TikTokRoomPinEvent> event);

    T onRoom(TikTokEventConsumer<TikTokRoomEvent> event);

    T onLivePaused(TikTokEventConsumer<TikTokLivePausedEvent> event);

    T onLike(TikTokEventConsumer<TikTokLikeEvent> event);

    T onBarrage(TikTokEventConsumer<TikTokBarrageEvent> event);

    T onGift(TikTokEventConsumer<TikTokGiftEvent> event);

    T onLinkMicArmies(TikTokEventConsumer<TikTokLinkMicArmiesEvent> event);

    T onEmote(TikTokEventConsumer<TikTokEmoteEvent> event);

    T onUnauthorizedMember(TikTokEventConsumer<TikTokUnauthorizedMemberEvent> event);

    T onInRoomBanner(TikTokEventConsumer<TikTokInRoomBannerEvent> event);

    T onLinkMicMethod(TikTokEventConsumer<TikTokLinkMicMethodEvent> event);

    T onSubscribe(TikTokEventConsumer<TikTokSubscribeEvent> event);

    T onPoll(TikTokEventConsumer<TikTokPollEvent> event);

    T onFollow(TikTokEventConsumer<TikTokFollowEvent> event);

    T onRoomViewerData(TikTokEventConsumer<TikTokRoomViewerDataEvent> event);

    T onGoalUpdate(TikTokEventConsumer<TikTokGoalUpdateEvent> event);

    T onComment(TikTokEventConsumer<TikTokCommentEvent> event);

    T onRankUpdate(TikTokEventConsumer<TikTokRankUpdateEvent> event);

    T onIMDelete(TikTokEventConsumer<TikTokIMDeleteEvent> event);

    T onLiveEnded(TikTokEventConsumer<TikTokLiveEndedEvent> event);

    T onError(TikTokEventConsumer<TikTokErrorEvent> event);

    T onUnhandled(TikTokEventConsumer<TikTokUnhandledWebsocketMessageEvent> event);

    T onJoin(TikTokEventConsumer<TikTokJoinEvent> event);

    T onRankText(TikTokEventConsumer<TikTokRankTextEvent> event);

    T onShare(TikTokEventConsumer<TikTokShareEvent> event);

    T onUnhandledMember(TikTokEventConsumer<TikTokUnhandledMemberEvent> event);

    T onSubNotify(TikTokEventConsumer<TikTokSubNotifyEvent> event);

    T onLinkMicBattle(TikTokEventConsumer<TikTokLinkMicBattleEvent> event);

    T onDisconnected(TikTokEventConsumer<TikTokDisconnectedEvent> event);

    T onGiftCombo(TikTokEventConsumer<TikTokGiftComboFinishedEvent> event);

    T onUnhandledControl(TikTokEventConsumer<TikTokUnhandledControlEvent> event);

    T onEvent(TikTokEventConsumer<TikTokEvent> event);

    T onWebsocketMessage(TikTokEventConsumer<TikTokWebsocketMessageEvent> event);

    T onReconnecting(TikTokEventConsumer<TikTokReconnectingEvent> event);

}


