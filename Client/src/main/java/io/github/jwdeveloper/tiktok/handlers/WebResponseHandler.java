package io.github.jwdeveloper.tiktok.handlers;

import io.github.jwdeveloper.tiktok.TikTokGiftManager;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.messages.*;
import io.github.jwdeveloper.tiktok.events.objects.TikTokGift;
import io.github.jwdeveloper.tiktok.messages.*;
import io.github.jwdeveloper.tiktok.models.GiftId;
import io.github.jwdeveloper.tiktok.models.SocialTypes;
import lombok.SneakyThrows;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WebResponseHandler extends WebResponseHandlerBase {
    private final TikTokGiftManager giftManager;

    public WebResponseHandler(TikTokEventHandler tikTokEventHandler, TikTokGiftManager giftManager) {
        super(tikTokEventHandler);
        this.giftManager = giftManager;
    }

    @Override
    public void init() {

        //ConnectionEvents events
        register(WebcastControlMessage.class, TikTokRoomMessageEvent.class);
        register(SystemMessage.class, this::handleWebcastControlMessage);


        //Room status events
        register(WebcastLiveIntroMessage.class, TikTokRoomMessageEvent.class);
        register(WebcastRoomUserSeqMessage.class, TikTokRoomViewerDataEvent.class); //TODO update viewer count    ViewerCount = userSeqMessage.ViewerCount;
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
    private TikTokEvent handleWebcastControlMessage(WebcastResponse.Message msg)
    {
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
        var giftId = new GiftId(giftMessage.getGiftId(), giftMessage.getSender().getUniqueId());

        var activeGifts = giftManager.getActiveGifts();
        if (activeGifts.containsKey(giftId)) {
            //   Debug.Log($"Updating Gift[{giftId.Gift}]Amount[{message.Amount}]");
            var gift = activeGifts.get(giftId);
            gift.setAmount(giftMessage.getAmount());
        } else {
            TikTokGift newGift = new TikTokGift(giftMessage);
            activeGifts.put(giftId, newGift);
            //   Debug.Log($"New Gift[{giftId.Gift}]Amount[{message.Amount}]");
            //    RunEvent(OnGift, newGift);
        }
        if (giftMessage.getRepeatEnd()) {
            //if (ShouldLog(LogLevel.Verbose))
            //   Debug.Log($"GiftStreak Ended: [{giftId.Gift}] Amount[{message.Amount}]")
            var gift = activeGifts.get(giftId);
            gift.setStreakFinished(true);
            activeGifts.remove(gift);
        }

        //   Debug.Log($"Handling GiftMessage");

        return new TikTokGiftMessageEvent(giftMessage);
    }

    @SneakyThrows
    private TikTokEvent handleSocialMedia(WebcastResponse.Message msg) {
        var message = WebcastSocialMessage.parseFrom(msg.getBinary());

        String type = message.getHeader().getSocialData().getType();
        Pattern pattern = Pattern.compile("pm_mt_guidance_viewer_([0-9]+)_share");
        Matcher matcher = pattern.matcher(type);
        if (matcher.find()) {
            var value = matcher.group(0);
            var number = Integer.parseInt(value);
            return new TikTokShareEvent(message, number);
        }

        var socialType = message.getHeader().getSocialData().getType();
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
}
