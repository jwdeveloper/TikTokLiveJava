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
import io.github.jwdeveloper.tiktok.data.events.http.TikTokHttpResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomInfoEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokShareEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketMessageEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketUnhandledMessageEvent;


public interface EventsBuilder<T> {

    /**
     * Invoked whenever specified event is triggered
     *
     * @param eventClass event class
     * @param action     action
     */
    <E extends TikTokEvent> T onEvent(Class<E> eventClass, EventConsumer<E> action);


    /**
     * Invoked whenever any event is triggered
     *
     * @param action
     * @return
     */
    T onEvent(EventConsumer<TikTokEvent> action);

    /**
     * Invoked when information about room (live) got updated such as viewer count, etc..
     *
     * @param action
     * @return
     */
    T onRoomInfo(EventConsumer<TikTokRoomInfoEvent> action);

    /**
     * Invoked when someone send message to chat
     *
     * @param action
     * @return
     */
    T onComment(EventConsumer<TikTokCommentEvent> action);


    /**
     * Invoked when TikTokLiveJava makes http request and getting response
     *
     * @param action
     * @return
     */
    T onHttpResponse(EventConsumer<TikTokHttpResponseEvent> action);

    /**
     * Invoked when TikTok protocolBuffer data "message" was successfully mapped to event
     * events contains protocol-buffer "Message"  and  TikTokLiveJava "Event"
     *
     * @param action
     * @return
     */
    T onWebsocketMessage(EventConsumer<TikTokWebsocketMessageEvent> action);

    /**
     * Invoked when there was not found event mapper for TikTok protocolBuffer data "message"
     *
     * @param action
     * @return
     */
    T onWebsocketUnhandledMessage(EventConsumer<TikTokWebsocketUnhandledMessageEvent> action);

    /**
     * Invoked every time TikTok sends protocolBuffer data to websocket
     * Response contains list of messages that are later mapped to events
     * @param action
     * @return
     */
    T onWebsocketResponse(EventConsumer<TikTokWebsocketResponseEvent> action);


    /**
     * Invoked for gifts that has no combo, or when combo finishes
     * @param action
     * @return
     */
    T onGift(EventConsumer<TikTokGiftEvent> action);

    /**
     * Invoked for gifts that has combo options such as roses
     * @param action
     * @return
     */
    T onGiftCombo(EventConsumer<TikTokGiftComboEvent> action);


    T onQuestion(EventConsumer<TikTokQuestionEvent> action);

    T onSubscribe(EventConsumer<TikTokSubscribeEvent> action);

    T onFollow(EventConsumer<TikTokFollowEvent> action);

    T onLike(EventConsumer<TikTokLikeEvent> action);

    T onEmote(EventConsumer<TikTokEmoteEvent> action);

    T onJoin(EventConsumer<TikTokJoinEvent> action);

    T onShare(EventConsumer<TikTokShareEvent> action);

    T onLivePaused(EventConsumer<TikTokLivePausedEvent> action);

    T onLiveUnpaused(EventConsumer<TikTokLiveUnpausedEvent> action);

    T onLiveEnded(EventConsumer<TikTokLiveEndedEvent> action);

    /**
     * Invoked when client has been successfully connected to live
     * @param action
     * @return
     */
    T onConnected(EventConsumer<TikTokConnectedEvent> action);

    /**
     * Invoked when client tries to reconnect
     * @param action
     * @return
     */
    T onReconnecting(EventConsumer<TikTokReconnectingEvent> action);

    /**
     * Invoked when client disconnected
     * @param action
     * @return
     */
    T onDisconnected(EventConsumer<TikTokDisconnectedEvent> action);

    /**
     * Invoked when exception was throed inside client or event handler
     * @param action
     * @return
     */
    T onError(EventConsumer<TikTokErrorEvent> action);


    // TODO Figure out how those events works
    //  T onChest(EventConsumer<TikTokChestEvent> event);

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


