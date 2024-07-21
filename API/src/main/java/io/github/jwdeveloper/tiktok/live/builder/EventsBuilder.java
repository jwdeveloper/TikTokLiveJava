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

import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.control.TikTokConnectingEvent;
import io.github.jwdeveloper.tiktok.data.events.control.TikTokPreConnectionEvent;
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
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftComboStateType;


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
     * @param action consumable action
     * @return self instance
     */
    default T onEvent(EventConsumer<TikTokEvent> action) {
        return onEvent(TikTokEvent.class, action);
    }


    /**
     * As a first event after method `LiveClient::connect()` is performed
     *
     * @param action consumable action
     * @return self instance
     */
    default T onConnecting(EventConsumer<TikTokConnectingEvent> action)
    {
        return onEvent(TikTokConnectingEvent.class, action);
    }

    /**
     * Invoked when information about room (live) got updated such as viewer count, etc..
     *
     * @param action consumable action
     * @return self instance
     */
    default T onRoomInfo(EventConsumer<TikTokRoomInfoEvent> action) {
        return onEvent(TikTokRoomInfoEvent.class, action);
    }

    /**
     * Invoked when someone send message to chat
     *
     * @param action consumable action
     * @return self instance
     */
    default T onComment(EventConsumer<TikTokCommentEvent> action) {
        return onEvent(TikTokCommentEvent.class, action);
    }

    /**
     * Invoked when TikTokLiveJava makes http request and getting response
     *
     * @param action consumable action
     * @return self instance
     */
    default T onHttpResponse(EventConsumer<TikTokHttpResponseEvent> action) {
        return onEvent(TikTokHttpResponseEvent.class, action);
    }

    /**
     * Invoked when TikTok protocolBuffer data "message" was successfully mapped to event
     * events contains protocol-buffer "Message"  and  TikTokLiveJava "Event"
     *
     * @param action consumable action
     * @return self instance
     */
    default T onWebsocketMessage(EventConsumer<TikTokWebsocketMessageEvent> action) {
        return onEvent(TikTokWebsocketMessageEvent.class, action);
    }

    /**
     * Triggered every time a protobuf encoded webcast message arrives. You can deserialize the binary object depending on the use case.
     *
     * @param action consumable action
     * @return self instance
     */
    default T onWebsocketUnhandledMessage(EventConsumer<TikTokWebsocketUnhandledMessageEvent> action) {
        return onEvent(TikTokWebsocketUnhandledMessageEvent.class, action);
    }

    /**
     * Invoked every time TikTok sends protocolBuffer data to websocket
     * Response contains list of messages that are later mapped to events
     *
     * @param action consumable action
     * @return self instance
     */
    default T onWebsocketResponse(EventConsumer<TikTokWebsocketResponseEvent> action) {
        return onEvent(TikTokWebsocketResponseEvent.class, action);
    }

    /**
     * Triggers for these different reasons:
     * <ol>
     *     <li>User sends gifts that have no combo (most of expensive gifts)</li>
     *     <li>{@link TikTokGiftComboEvent} has combaState = {@link GiftComboStateType#Finished}</li>
     * </ol>
     * @param action consumable action
     * @return self instance
     */
    default T onGift(EventConsumer<TikTokGiftEvent> action) {
        return onEvent(TikTokGiftEvent.class, action);
    }

    /**
     * Triggered every time a gift is sent
     * <p>Example when user sends gift with combo</p>
     * <ul>
     *     <li>Combo: 1  -> comboState = {@link GiftComboStateType#Begin}</li>
     *     <li>Combo: 4 -> comboState = {@link GiftComboStateType#Active}</li>
     *     <li>Combo: 8 -> comboState = {@link GiftComboStateType#Active}</li>
     *     <li>Combo: 12 -> comboState = {@link GiftComboStateType#Finished}</li>
     * </ul>
     * Both {@link TikTokGiftComboEvent} and {@link TikTokGiftEvent} events are triggered when comboState is Finished
     *
     * @apiNote {@link GiftComboStateType} has 3 states: {@link GiftComboStateType#Begin Begin}, {@link GiftComboStateType#Active Active}, & {@link GiftComboStateType#Finished Finished}
     * @param action consumable action
     * @return self instance
     */
    default T onGiftCombo(EventConsumer<TikTokGiftComboEvent> action) {
        return onEvent(TikTokGiftComboEvent.class, action);
    }

    /**
     * Triggered every time someone asks a new question via the question feature.
     *
     * @param action consumable action
     * @return self instance
     */
    default T onQuestion(EventConsumer<TikTokQuestionEvent> action) {
        return onEvent(TikTokQuestionEvent.class, action);
    }

    /**
     * Triggers when a user subscribe the streamer.
     *
     * @param action consumable action
     * @return self instance
     */
    default T onSubscribe(EventConsumer<TikTokSubscribeEvent> action) {
        return onEvent(TikTokSubscribeEvent.class, action);
    }

    /**
     * Triggers when a user follows the streamer. Based on social event.
     *
     * @param action consumable action
     * @return self instance
     */
    default T onFollow(EventConsumer<TikTokFollowEvent> action) {
        return onEvent(TikTokFollowEvent.class, action);
    }

    /**
     * Triggered when a viewer sends likes to the streamer. For streams with many viewers, this event is not always triggered by TikTok.
     *
     * @param action consumable action
     * @return self instance
     */
    default T onLike(EventConsumer<TikTokLikeEvent> action) {
        return onEvent(TikTokLikeEvent.class, action);
    }

    /**
     * Triggers when a user sends emote
     *
     * @param action consumable action
     * @return self instance
     */
    default T onEmote(EventConsumer<TikTokEmoteEvent> action) {
        return onEvent(TikTokEmoteEvent.class, action);
    }

    /**
     * Triggers when a user joins to the live
     *
     * @param action consumable action
     * @return self instance
     */
    default T onJoin(EventConsumer<TikTokJoinEvent> action) {
        return onEvent(TikTokJoinEvent.class, action);
    }

    /**
     * Triggers when a user shares the stream.
     *
     * @param action consumable action
     * @return self instance
     */
    default T onShare(EventConsumer<TikTokShareEvent> action) {
        return onEvent(TikTokShareEvent.class, action);
    }

    /**
     * Triggered when the live stream gets paused
     *
     * @param action consumable action
     * @return self instance
     */
    default T onLivePaused(EventConsumer<TikTokLivePausedEvent> action) {
        return onEvent(TikTokLivePausedEvent.class, action);
    }

    /**
     * Triggered when the live stream gets unpaused
     *
     * @param action consumable action
     * @return self instance
     */
    default T onLiveUnpaused(EventConsumer<TikTokLiveUnpausedEvent> action) {
        return onEvent(TikTokLiveUnpausedEvent.class, action);
    }

    /**
     * Triggered when the live stream gets terminated by the host. Will also trigger the TikTokDisconnectedEvent event.
     *
     * @param action consumable action
     * @return self instance
     */
    default T onLiveEnded(EventConsumer<TikTokLiveEndedEvent> action) {
        return onEvent(TikTokLiveEndedEvent.class, action);
    }

    /**
     * Invoked when client has been successfully connected to live
     *
     * @param action consumable action
     * @return self instance
     */
    default T onConnected(EventConsumer<TikTokConnectedEvent> action) {
        return onEvent(TikTokConnectedEvent.class, action);
    }

    /**
     * Invoked before client has been successfully connected to live
     *
     * @param action consumable action
     * @return self instance
     */
    default T onPreConnection(EventConsumer<TikTokPreConnectionEvent> action) {
        return onEvent(TikTokPreConnectionEvent.class, action);
    }

    /**
     * Invoked when client tries to reconnect
     *
     * @param action consumable action
     * @return self instance
     */
    default T onReconnecting(EventConsumer<TikTokReconnectingEvent> action) {
        return onEvent(TikTokReconnectingEvent.class, action);
    }

    /**
     * Triggered when the connection gets disconnected. In that case you can call connect() again to have a reconnect logic.
     * Note that you should wait a little bit before attempting a reconnect to avoid being rate-limited.
     *
     * @param action consumable action
     * @return self instance
     */
    default T onDisconnected(EventConsumer<TikTokDisconnectedEvent> action) {
        return onEvent(TikTokDisconnectedEvent.class, action);
    }

    /**
     * Invoked when exception was throed inside client or event handler
     *
     * @param action consumable action
     * @return self instance
     */
    default T onError(EventConsumer<TikTokErrorEvent> action) {
        return onEvent(TikTokErrorEvent.class, action);
    }


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

    /**
     * To do figure out how to use Annotation processor.
     * Goal is to generates methods for all possible events, everytime library is compiled
     */

}