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
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.messages.*;
import io.github.jwdeveloper.tiktok.events.objects.Gift;
import io.github.jwdeveloper.tiktok.gifts.TikTokGiftManager;
import io.github.jwdeveloper.tiktok.messages.*;
import io.github.jwdeveloper.tiktok.models.SocialTypes;
import lombok.SneakyThrows;

import java.util.regex.Pattern;

public class TikTokMessageHandlerRegistration extends TikTokMessageHandler {
    private final TikTokGiftManager giftManager;
    private final TikTokRoomInfo roomInfo;

    private final Pattern socialMediaPattern = Pattern.compile("pm_mt_guidance_viewer_([0-9]+)_share");

    public TikTokMessageHandlerRegistration(TikTokEventObserver tikTokEventHandler,
                                            TikTokGiftManager giftManager,
                                            TikTokRoomInfo roomInfo) {
        super(tikTokEventHandler);
        this.giftManager = giftManager;
        this.roomInfo = roomInfo;
    }

    @Override
    public void init() {

        //ConnectionEvents events
        registerMapping(WebcastControlMessage.class, this::handleWebcastControlMessage);
        registerMapping(SystemMessage.class, TikTokRoomEvent.class);


        //Room status events
        registerMapping(WebcastLiveIntroMessage.class, TikTokRoomEvent.class);
        registerMapping(WebcastRoomUserSeqMessage.class, this::handleRoomUserSeqMessage);
        registerMapping(RoomMessage.class, TikTokRoomEvent.class);
        registerMapping(WebcastRoomMessage.class, TikTokRoomEvent.class);
        registerMapping(WebcastCaptionMessage.class, TikTokCaptionEvent.class);

        //User Interactions events
        registerMapping(WebcastChatMessage.class, TikTokCommentEvent.class);
        registerMapping(WebcastLikeMessage.class, TikTokLikeEvent.class);
        registerMapping(WebcastGiftMessage.class, this::handleGift);
        registerMapping(WebcastSocialMessage.class, this::handleSocialMedia);
        registerMapping(WebcastMemberMessage.class, this::handleMemberMessage);

        //Host Interaction events
        registerMapping(WebcastPollMessage.class, TikTokPollEvent.class);
        registerMapping(WebcastRoomPinMessage.class, TikTokRoomPinEvent.class);
        registerMapping(WebcastGoalUpdateMessage.class, TikTokGoalUpdateEvent.class);

        //LinkMic events
        registerMapping(WebcastLinkMicBattle.class, TikTokLinkMicBattleEvent.class);
        registerMapping(WebcastLinkMicArmies.class, TikTokLinkMicArmiesEvent.class);
        registerMapping(LinkMicMethod.class, TikTokLinkMicMethodEvent.class);
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
    private TikTokEvent handleWebcastControlMessage(WebcastResponse.Message msg) {
        var message = WebcastControlMessage.parseFrom(msg.getBinary());
        return switch (message.getAction()) {
            case STREAM_PAUSED -> new TikTokLivePausedEvent();
            case STREAM_ENDED -> new TikTokLiveEndedEvent();
            default -> new TikTokUnhandledControlEvent(message);
        };
    }

    @SneakyThrows
    private TikTokEvent handleGift(WebcastResponse.Message msg) {
        var giftMessage = WebcastGiftMessage.parseFrom(msg.getBinary());
        giftManager.updateActiveGift(giftMessage);

        var gift = giftManager.findById((int) giftMessage.getGiftId());
        if (gift == Gift.UNDEFINED) {
            gift = giftManager.findByName(giftMessage.getGift().getName());
        }
        if (gift == Gift.UNDEFINED) {
            gift = giftManager.registerGift(
                    (int) giftMessage.getGift().getId(),
                    giftMessage.getGift().getName(),
                    giftMessage.getGift().getDiamondCount());
        }

        if (giftMessage.getRepeatEnd() > 0) {
            return new TikTokGiftComboFinishedEvent(gift, giftMessage);
        }

        return new TikTokGiftEvent(gift, giftMessage);
    }

    @SneakyThrows
    private TikTokEvent handleSocialMedia(WebcastResponse.Message msg) {
        var message = WebcastSocialMessage.parseFrom(msg.getBinary());

        var socialType = message.getHeader().getSocialData().getType();
        var matcher = socialMediaPattern.matcher(socialType);

        if (matcher.find()) {
            var value = matcher.group(1);
            var number = Integer.parseInt(value);
            return new TikTokShareEvent(message, number);
        }

        return switch (socialType) {
            case SocialTypes.LikeType -> new TikTokLikeEvent(message);
            case SocialTypes.FollowType -> new TikTokFollowEvent(message);
            case SocialTypes.ShareType -> new TikTokShareEvent(message);
            case SocialTypes.JoinType -> new TikTokJoinEvent(message);
            default -> new TikTokUnhandledSocialEvent(message);
        };
    }

    @SneakyThrows
    private TikTokEvent handleMemberMessage(WebcastResponse.Message msg) {
        var message = WebcastMemberMessage.parseFrom(msg.getBinary());
        return switch (message.getAction()) {
            case JOINED -> new TikTokJoinEvent(message);
            case SUBSCRIBED -> new TikTokSubscribeEvent(message);
            default -> new TikTokUnhandledMemberEvent(message);
        };
    }

    private TikTokEvent handleRoomUserSeqMessage(WebcastResponse.Message msg) {
        var event = (TikTokRoomViewerDataEvent) mapMessageToEvent(WebcastRoomUserSeqMessage.class, TikTokRoomViewerDataEvent.class, msg);
        roomInfo.setViewersCount(event.getViewerCount());
        return event;
    }
}
