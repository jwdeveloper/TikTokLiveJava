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
package io.github.jwdeveloper.tiktok.live.builder;

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftComboEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomInfoEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokShareEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketMessageEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketUnhandledMessageEvent;


public interface EventsBuilder<T> {

    T onRoomInfo(EventConsumer<TikTokRoomInfoEvent> event);

    T onComment(EventConsumer<TikTokCommentEvent> event);

    T onWebsocketMessage(EventConsumer<TikTokWebsocketMessageEvent> event);

    T onWebsocketResponse(EventConsumer<TikTokWebsocketResponseEvent> event);

    T onWebsocketUnhandledMessage(EventConsumer<TikTokWebsocketUnhandledMessageEvent> event);


    T onGiftCombo(EventConsumer<TikTokGiftComboEvent> event);
    T onGift(EventConsumer<TikTokGiftEvent> event);

    T onQuestion(EventConsumer<TikTokQuestionEvent> event);

    T onSubscribe(EventConsumer<TikTokSubscribeEvent> event);

    T onFollow(EventConsumer<TikTokFollowEvent> event);

    T onLike(EventConsumer<TikTokLikeEvent> event);

    T onEmote(EventConsumer<TikTokEmoteEvent> event);

    T onJoin(EventConsumer<TikTokJoinEvent> event);

    T onShare(EventConsumer<TikTokShareEvent> event);

  //  T onChest(EventConsumer<TikTokChestEvent> event);

    T onLivePaused(EventConsumer<TikTokLivePausedEvent> event);

    T onLiveUnpaused(EventConsumer<TikTokLiveUnpausedEvent> event);

    T onLiveEnded(EventConsumer<TikTokLiveEndedEvent> event);

    T onConnected(EventConsumer<TikTokConnectedEvent> event);

    T onReconnecting(EventConsumer<TikTokReconnectingEvent> event);

    T onDisconnected(EventConsumer<TikTokDisconnectedEvent> event);

    T onError(EventConsumer<TikTokErrorEvent> event);
    T onEvent(EventConsumer<TikTokEvent> event);




    // TODO Figure out how those events works
    //T onLinkMicFanTicket(TikTokEventConsumer<TikTokLinkMicFanTicketEvent> event);

    //T onEnvelope(TikTokEventConsumer<TikTokEnvelopeEvent> event);

    //T onShop(TikTokEventConsumer<TikTokShopEvent> event);

    //T onDetect(TikTokEventConsumer<TikTokDetectEvent> event);

    //T onLinkLayer(TikTokEventConsumer<TikTokLinkLayerEvent> event);

    //T onCaption(TikTokEventConsumer<TikTokCaptionEvent> event);

    //T onRoomPin(TikTokEventConsumer<TikTokRoomPinEvent> event);

    //T onBarrage(TikTokEventConsumer<TikTokBarrageEvent> event);

    //T onLinkMicArmies(TikTokEventConsumer<TikTokLinkMicArmiesEvent> event);

    //T onUnauthorizedMember(TikTokEventConsumer<TikTokUnauthorizedMemberEvent> event);

    //T onInRoomBanner(TikTokEventConsumer<TikTokInRoomBannerEvent> event);

    //T onLinkMicMethod(TikTokEventConsumer<TikTokLinkMicMethodEvent> event);

    //T onPoll(TikTokEventConsumer<TikTokPollEvent> event);

    //T onGoalUpdate(TikTokEventConsumer<TikTokGoalUpdateEvent> event);

    //T onRankUpdate(TikTokEventConsumer<TikTokRankUpdateEvent> event);

    //T onIMDelete(TikTokEventConsumer<TikTokIMDeleteEvent> event);

    //T onRankText(TikTokEventConsumer<TikTokRankTextEvent> event);

    //T onUnhandledMember(TikTokEventConsumer<TikTokUnhandledMemberEvent> event);

    //T onSubNotify(TikTokEventConsumer<TikTokSubNotifyEvent> event);

    //T onLinkMicBattle(TikTokEventConsumer<TikTokLinkMicBattleEvent> event);

    //T onUnhandledControl(TikTokEventConsumer<TikTokUnhandledControlEvent> event);
}


