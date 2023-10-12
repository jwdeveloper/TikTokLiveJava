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

import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollEndEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollStartEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollUpdateEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomPinEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomUserInfoEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokShareEvent;
import io.github.jwdeveloper.tiktok.data.models.Text;
import io.github.jwdeveloper.tiktok.handlers.events.TikTokGiftEventHandler;
import io.github.jwdeveloper.tiktok.mappers.TikTokGenericEventMapper;
import io.github.jwdeveloper.tiktok.messages.webcast.*;
import io.github.jwdeveloper.tiktok.models.SocialTypes;
import lombok.SneakyThrows;

import java.util.regex.Pattern;

public class TikTokMessageHandlerRegistration extends TikTokMessageHandler {

    private final TikTokRoomInfo roomInfo;
    private final TikTokGiftEventHandler giftHandler;
    private final Pattern socialMediaPattern = Pattern.compile("pm_mt_guidance_viewer_([0-9]+)_share");

    public TikTokMessageHandlerRegistration(TikTokEventObserver tikTokEventHandler,
                                            TikTokRoomInfo roomInfo,
                                            TikTokGenericEventMapper genericTikTokEventMapper,
                                            TikTokGiftEventHandler tikTokGiftEventHandler) {
        super(tikTokEventHandler, genericTikTokEventMapper);
        this.giftHandler = tikTokGiftEventHandler;
        this.roomInfo = roomInfo;
        init();
    }

    public void init() {

        //ConnectionEvents events
        registerMapping(WebcastControlMessage.class, this::handleWebcastControlMessage);
        registerMapping(WebcastSystemMessage.class, TikTokRoomEvent.class);


        //Room status events
        registerMapping(WebcastLiveIntroMessage.class, TikTokRoomEvent.class);
        registerMapping(WebcastRoomUserSeqMessage.class, this::handleRoomUserSeqMessage);
        registerMapping(RoomMessage.class, TikTokRoomEvent.class);
        registerMapping(WebcastRoomMessage.class, TikTokRoomEvent.class);
        registerMapping(WebcastCaptionMessage.class, TikTokCaptionEvent.class);

        //User Interactions events
        registerMapping(WebcastChatMessage.class, TikTokCommentEvent.class);
        registerMapping(WebcastLikeMessage.class, this::handleLike);
        registerMappings(WebcastGiftMessage.class, giftHandler::handleGift);
        registerMapping(WebcastSocialMessage.class, this::handleSocialMedia);
        registerMapping(WebcastMemberMessage.class, this::handleMemberMessage);

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
        registerMapping(WebcastEnvelopeMessage.class, TikTokEnvelopeEvent.class);
        registerMapping(WebcastSubNotifyMessage.class, TikTokSubNotifyEvent.class);
        registerMapping(WebcastEmoteChatMessage.class, TikTokEmoteEvent.class);
    }


    @SneakyThrows
    private TikTokEvent handleWebcastControlMessage(byte[] msg) {
        var message = WebcastControlMessage.parseFrom(msg);
        return switch (message.getAction()) {
            case STREAM_PAUSED -> new TikTokLivePausedEvent();
            case STREAM_ENDED -> new TikTokLiveEndedEvent();
            default -> new TikTokUnhandledControlEvent(message);
        };
    }


    @SneakyThrows
    private TikTokEvent handleSocialMedia(byte[] msg) {
        var message = WebcastSocialMessage.parseFrom(msg);

        var socialType = Text.map(message.getCommon().getDisplayText()).getKey();
        var matcher = socialMediaPattern.matcher(socialType);

        if (matcher.find()) {
            var value = matcher.group(1);
            var number = Integer.parseInt(value);
            return new TikTokShareEvent(message, number);
        }

        return switch (socialType) {
            case SocialTypes.LikeType -> new TikTokLikeEvent(message, roomInfo.getLikesCount());
            case SocialTypes.FollowType -> new TikTokFollowEvent(message);
            case SocialTypes.ShareType -> new TikTokShareEvent(message);
            case SocialTypes.JoinType -> new TikTokJoinEvent(message, roomInfo.getViewersCount());
            default -> new TikTokUnhandledSocialEvent(message);
        };
    }

    @SneakyThrows
    private TikTokEvent handleMemberMessage(byte[] msg) {
        var message = WebcastMemberMessage.parseFrom(msg);
        return switch (message.getAction()) {
            case JOINED -> new TikTokJoinEvent(message);
            case SUBSCRIBED -> new TikTokSubscribeEvent(message);
            default -> new TikTokUnhandledMemberEvent(message);
        };
    }

    private TikTokEvent handleRoomUserSeqMessage(byte[] msg) {
        var event = (TikTokRoomUserInfoEvent) mapper.mapToEvent(WebcastRoomUserSeqMessage.class, TikTokRoomUserInfoEvent.class, msg);
        roomInfo.setViewersCount(event.getTotalUsers());
        return event;
    }

    private TikTokEvent handleLike(byte[] msg) {
        var event = (TikTokLikeEvent) mapper.mapToEvent(WebcastLikeMessage.class, TikTokLikeEvent.class, msg);
        roomInfo.setLikesCount(event.getTotalLikes());
        return event;
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


}
