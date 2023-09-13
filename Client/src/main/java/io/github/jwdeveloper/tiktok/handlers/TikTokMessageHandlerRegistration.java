package io.github.jwdeveloper.tiktok.handlers;

import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.TikTokGiftManager;
import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.messages.*;
import io.github.jwdeveloper.tiktok.messages.*;
import io.github.jwdeveloper.tiktok.models.SocialTypes;
import lombok.SneakyThrows;

import java.util.logging.Logger;
import java.util.regex.Pattern;

public class TikTokMessageHandlerRegistration extends TikTokMessageHandler {
    private final TikTokGiftManager giftManager;
    private final TikTokRoomInfo roomInfo;

    public TikTokMessageHandlerRegistration(TikTokEventObserver tikTokEventHandler,
                                            ClientSettings clientSettings,
                                            Logger logger,
                                            TikTokGiftManager giftManager,
                                            TikTokRoomInfo roomInfo) {
        super(tikTokEventHandler, clientSettings, logger);
        this.giftManager = giftManager;
        this.roomInfo = roomInfo;
    }

    @Override
    public void init() {

        //ConnectionEvents events
        register(WebcastControlMessage.class,  this::handleWebcastControlMessage);
        register(SystemMessage.class,TikTokRoomMessageEvent.class);


        //Room status events
        register(WebcastLiveIntroMessage.class, TikTokRoomMessageEvent.class);
        register(WebcastRoomUserSeqMessage.class, this::handleRoomUserSeqMessage);
        register(RoomMessage.class, TikTokRoomMessageEvent.class);
        register(WebcastRoomMessage.class, TikTokRoomMessageEvent.class);
        register(WebcastCaptionMessage.class, TikTokCaptionEvent.class);

        //User Interactions events
        register(WebcastChatMessage.class, TikTokCommentEvent.class);
        register(WebcastLikeMessage.class, TikTokLikeEvent.class);
        register(WebcastGiftMessage.class, this::handleGift);
        register(WebcastSocialMessage.class, this::handleSocialMedia);
        register(WebcastMemberMessage.class, this::handleMemberMessage);

        //Host Interaction events
        register(WebcastPollMessage.class, TikTokPollMessageEvent.class);
        register(WebcastRoomPinMessage.class, TikTokRoomPinMessageEvent.class);
        register(WebcastGoalUpdateMessage.class, TikTokGoalUpdateEvent.class);

        //LinkMic events
        register(WebcastLinkMicBattle.class, TikTokLinkMicBattleEvent.class);
        register(WebcastLinkMicArmies.class, TikTokLinkMicArmiesEvent.class);
        register(LinkMicMethod.class, TikTokLinkMicMethodEvent.class);
        register(WebcastLinkMicMethod.class, TikTokLinkMicMethodEvent.class);
        register(WebcastLinkMicFanTicketMethod.class, TikTokLinkMicFanTicketEvent.class);

        //Rank events
        register(WebcastRankTextMessage.class, TikTokRankTextEvent.class);
        register(WebcastRankUpdateMessage.class, TikTokRankUpdateEvent.class);
        register(WebcastHourlyRankMessage.class, TikTokRankUpdateEvent.class);

        //Others events
        register(WebcastInRoomBannerMessage.class, TikTokInRoomBannerEvent.class);
        register(WebcastMsgDetectMessage.class, TikTokDetectMessageEvent.class);
        register(WebcastBarrageMessage.class, TikTokBarrageMessageEvent.class);
        register(WebcastUnauthorizedMemberMessage.class, TikTokUnauthorizedMemberEvent.class);
        register(WebcastGiftBroadcastMessage.class, TikTokGiftBroadcastEvent.class);
        register(WebcastOecLiveShoppingMessage.class, TikTokShopMessageEvent.class);
        register(WebcastImDeleteMessage.class, TikTokIMDeleteEvent.class);
        register(WebcastQuestionNewMessage.class, TikTokQuestionEvent.class);
        register(WebcastEnvelopeMessage.class, TikTokEnvelopeEvent.class);
        register(WebcastSubNotifyMessage.class, TikTokSubNotifyEvent.class);
        register(WebcastEmoteChatMessage.class, TikTokEmoteEvent.class);
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
        return new TikTokGiftMessageEvent(giftMessage);
    }

    @SneakyThrows
    private TikTokEvent handleSocialMedia(WebcastResponse.Message msg) {
        var message = WebcastSocialMessage.parseFrom(msg.getBinary());

        var socialType = message.getHeader().getSocialData().getType();
        var pattern = Pattern.compile("pm_mt_guidance_viewer_([0-9]+)_share");
        var matcher = pattern.matcher(socialType);

        if (matcher.find())
        {
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

    private TikTokEvent handleRoomUserSeqMessage(WebcastResponse.Message msg)
    {
        var event = (TikTokRoomViewerDataEvent)mapMessageToEvent(WebcastRoomUserSeqMessage.class, TikTokRoomViewerDataEvent.class, msg);
        roomInfo.setViewersCount(event.getViewerCount());
        return event;
    }
}
