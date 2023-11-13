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
package io.github.jwdeveloper.tiktok.handlers;

import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.envelop.TikTokChestEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollEndEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollStartEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollUpdateEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomPinEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.models.chest.Chest;
import io.github.jwdeveloper.tiktok.handlers.events.TikTokGiftEventHandler;
import io.github.jwdeveloper.tiktok.handlers.events.TikTokRoomInfoEventHandler;
import io.github.jwdeveloper.tiktok.handlers.events.TikTokSocialMediaEventHandler;
import io.github.jwdeveloper.tiktok.mappers.TikTokGenericEventMapper;
import io.github.jwdeveloper.tiktok.messages.enums.EnvelopeDisplay;
import io.github.jwdeveloper.tiktok.messages.webcast.*;
import lombok.SneakyThrows;

import java.util.Collections;
import java.util.List;

public class TikTokMessageHandlerRegistration extends TikTokMessageHandler {

    private final TikTokGiftEventHandler giftHandler;
    private final TikTokRoomInfoEventHandler roomInfoHandler;
    private final TikTokSocialMediaEventHandler socialHandler;

    public TikTokMessageHandlerRegistration(TikTokEventObserver tikTokEventHandler,
                                            TikTokRoomInfoEventHandler roomInfoHandler,
                                            TikTokGenericEventMapper genericTikTokEventMapper,
                                            TikTokGiftEventHandler tikTokGiftEventHandler,
                                            TikTokSocialMediaEventHandler tikTokSocialMediaEventHandler) {
        super(tikTokEventHandler, genericTikTokEventMapper);
        this.giftHandler = tikTokGiftEventHandler;
        this.roomInfoHandler = roomInfoHandler;
        this.socialHandler = tikTokSocialMediaEventHandler;
        init();
    }

    public void init() {

        //ConnectionEvents events
        registerMapping(WebcastControlMessage.class, this::handleWebcastControlMessage);

        //Room status events
        registerMapping(WebcastLiveIntroMessage.class, roomInfoHandler::handleIntro);
        registerMapping(WebcastRoomUserSeqMessage.class, roomInfoHandler::handleUserRanking);

        registerMapping(WebcastCaptionMessage.class, TikTokCaptionEvent.class);

        //User Interactions events
        registerMapping(WebcastChatMessage.class, TikTokCommentEvent.class);
        registerMappings(WebcastLikeMessage.class, this::handleLike);
        registerMappings(WebcastGiftMessage.class, giftHandler::handleGift);
        registerMapping(WebcastSocialMessage.class, socialHandler::handle);
        registerMappings(WebcastMemberMessage.class, this::handleMemberMessage);

        //Host Interaction events
        registerMapping(WebcastPollMessage.class, this::handlePollEvent);
        registerMapping(WebcastRoomPinMessage.class, this::handlePinMessage);
        registerMapping(WebcastGoalUpdateMessage.class, TikTokGoalUpdateEvent.class);

        //LinkMic events
        registerMapping(WebcastLinkMicBattle.class, TikTokLinkMicBattleEvent.class);
        registerMapping(WebcastLinkMicArmies.class, TikTokLinkMicArmiesEvent.class);
        registerMapping(WebcastLinkMicMethod.class, TikTokLinkMicMethodEvent.class);
        registerMapping(WebcastLinkMicFanTicketMethod.class, TikTokLinkMicFanTicketEvent.class);

        //Rank events
        registerMapping(WebcastRankTextMessage.class, TikTokRankTextEvent.class);
        registerMapping(WebcastRankUpdateMessage.class, TikTokRankUpdateEvent.class);
        registerMapping(WebcastHourlyRankMessage.class, TikTokRankUpdateEvent.class);

        //Others events
        registerMapping(WebcastInRoomBannerMessage.class, TikTokInRoomBannerEvent.class);
        registerMapping(WebcastMsgDetectMessage.class, TikTokDetectEvent.class);
        registerMapping(WebcastBarrageMessage.class, TikTokBarrageEvent.class);
        registerMapping(WebcastUnauthorizedMemberMessage.class, TikTokUnauthorizedMemberEvent.class);
        registerMapping(WebcastOecLiveShoppingMessage.class, TikTokShopEvent.class);
        registerMapping(WebcastImDeleteMessage.class, TikTokIMDeleteEvent.class);
        registerMapping(WebcastQuestionNewMessage.class, TikTokQuestionEvent.class);
        registerMappings(WebcastEnvelopeMessage.class, this::handleEnvelop);
        registerMapping(WebcastSubNotifyMessage.class, TikTokSubNotifyEvent.class);
        registerMapping(WebcastEmoteChatMessage.class, TikTokEmoteEvent.class);
    }


    @SneakyThrows
    private TikTokEvent handleWebcastControlMessage(byte[] msg) {
        var message = WebcastControlMessage.parseFrom(msg);
        return switch (message.getAction()) {
            case STREAM_PAUSED -> new TikTokLivePausedEvent();
            case STREAM_ENDED -> new TikTokLiveEndedEvent();
            case STREAM_UNPAUSED -> new TikTokLiveUnpausedEvent();
            default -> new TikTokUnhandledControlEvent(message);
        };
    }


    @SneakyThrows
    private List<TikTokEvent> handleMemberMessage(byte[] msg) {
        var message = WebcastMemberMessage.parseFrom(msg);

        var event = switch (message.getAction()) {
            case JOINED -> new TikTokJoinEvent(message);
            case SUBSCRIBED -> new TikTokSubscribeEvent(message);
            default -> new TikTokUnhandledMemberEvent(message);
        };

        var roomInfoEvent = roomInfoHandler.handleRoomInfo(tikTokRoomInfo ->
        {
            tikTokRoomInfo.setViewersCount(message.getMemberCount());
        });

        return List.of(event, roomInfoEvent);
    }

    private List<TikTokEvent> handleLike(byte[] msg) {
        var event = (TikTokLikeEvent) mapper.mapToEvent(WebcastLikeMessage.class, TikTokLikeEvent.class, msg);
        var roomInfoEvent = roomInfoHandler.handleRoomInfo(tikTokRoomInfo ->
        {
            tikTokRoomInfo.setLikesCount(event.getTotalLikes());
        });
        return List.of(event, roomInfoEvent);
    }

    @SneakyThrows
    private TikTokEvent handlePinMessage(byte[] msg) {
        var pinMessage = WebcastRoomPinMessage.parseFrom(msg);
        var chatMessage = WebcastChatMessage.parseFrom(pinMessage.getPinnedMessage());
        var chatEvent = new TikTokCommentEvent(chatMessage);
        return new TikTokRoomPinEvent(pinMessage, chatEvent);
    }

    //TODO Probably not working
    @SneakyThrows
    private TikTokEvent handlePollEvent(byte[] msg) {
        var poolMessage = WebcastPollMessage.parseFrom(msg);
        return switch (poolMessage.getMessageType()) {
            case 0 -> new TikTokPollStartEvent(poolMessage);
            case 1 -> new TikTokPollEndEvent(poolMessage);
            case 2 -> new TikTokPollUpdateEvent(poolMessage);
            default -> new TikTokPollEvent(poolMessage);
        };
    }

    @SneakyThrows
    private List<TikTokEvent> handleEnvelop(byte[] data) {
        var msg = WebcastEnvelopeMessage.parseFrom(data);
        if (msg.getDisplay() != EnvelopeDisplay.EnvelopeDisplayNew) {
            return Collections.emptyList();
        }
        var totalDiamonds = msg.getEnvelopeInfo().getDiamondCount();
        var totalUsers = msg.getEnvelopeInfo().getPeopleCount();
        var chest = new Chest(totalDiamonds, totalUsers);

        return List.of(new TikTokChestEvent(chest, msg));
    }


}
