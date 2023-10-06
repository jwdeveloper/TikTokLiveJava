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
package io.github.jwdeveloper.tiktok.live.events;

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftComboFinishedEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomUserInfoEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokShareEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketMessageEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketUnhandledMessageEvent;


public interface TikTokEventBuilder<T> {

    T onRoom(TikTokEventConsumer<TikTokRoomEvent> event);

    T onRoomUserInfo(TikTokEventConsumer<TikTokRoomUserInfoEvent> event);

    T onComment(TikTokEventConsumer<TikTokCommentEvent> event);


    T onWebsocketMessage(TikTokEventConsumer<TikTokWebsocketMessageEvent> event);

    T onWebsocketResponse(TikTokEventConsumer<TikTokWebsocketResponseEvent> event);

    T onWebsocketUnhandledMessage(TikTokEventConsumer<TikTokWebsocketUnhandledMessageEvent> event);


    T onGiftCombo(TikTokEventConsumer<TikTokGiftComboFinishedEvent> event);

    T onGift(TikTokEventConsumer<TikTokGiftEvent> event);

    T onQuestion(TikTokEventConsumer<TikTokQuestionEvent> event);

    T onSubscribe(TikTokEventConsumer<TikTokSubscribeEvent> event);

    T onFollow(TikTokEventConsumer<TikTokFollowEvent> event);

    T onLike(TikTokEventConsumer<TikTokLikeEvent> event);

    T onEmote(TikTokEventConsumer<TikTokEmoteEvent> event);

    T onJoin(TikTokEventConsumer<TikTokJoinEvent> event);

    T onShare(TikTokEventConsumer<TikTokShareEvent> event);

    T onUnhandledSocial(TikTokEventConsumer<TikTokUnhandledSocialEvent> event);


    T onLivePaused(TikTokEventConsumer<TikTokLivePausedEvent> event);

    T onLiveEnded(TikTokEventConsumer<TikTokLiveEndedEvent> event);

    T onConnected(TikTokEventConsumer<TikTokConnectedEvent> event);

    T onReconnecting(TikTokEventConsumer<TikTokReconnectingEvent> event);

    T onDisconnected(TikTokEventConsumer<TikTokDisconnectedEvent> event);

    T onError(TikTokEventConsumer<TikTokErrorEvent> event);

    T onEvent(TikTokEventConsumer<TikTokEvent> event);


    // TODO implement later
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


