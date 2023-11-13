package io.github.jwdeveloper.tiktok.handlers.events;

import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.data.events.TikTokUnhandledSocialEvent;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokShareEvent;
import io.github.jwdeveloper.tiktok.data.models.Text;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastSocialMessage;
import io.github.jwdeveloper.tiktok.models.SocialTypes;
import lombok.SneakyThrows;
import java.util.regex.Pattern;

public class TikTokSocialMediaEventHandler
{
    private final TikTokRoomInfo roomInfo;
    private final Pattern socialMediaPattern = Pattern.compile("pm_mt_guidance_viewer_([0-9]+)_share");

    public TikTokSocialMediaEventHandler(TikTokRoomInfo roomInfo) {
        this.roomInfo = roomInfo;
    }

    @SneakyThrows
    public TikTokEvent handle(byte[] msg)
    {
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


}
