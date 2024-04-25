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
package io.github.jwdeveloper.tiktok.mappers.handlers;

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

import java.util.regex.Matcher;
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
        WebcastSocialMessage message = WebcastSocialMessage.parseFrom(msg);

        String socialType = Text.map(message.getCommon().getDisplayText()).getKey();
        Matcher matcher = socialMediaPattern.matcher(socialType);

        if (matcher.find()) {
            String value = matcher.group(1);
            int number = Integer.parseInt(value);
            return new TikTokShareEvent(message, number);
        }

        switch (socialType) {
            case SocialTypes.LikeType: return new TikTokLikeEvent(message, roomInfo.getLikesCount());
            case SocialTypes.FollowType: return new TikTokFollowEvent(message);
            case SocialTypes.ShareType: return new TikTokShareEvent(message);
            case SocialTypes.JoinType: return new TikTokJoinEvent(message, roomInfo.getViewersCount());
            default: return new TikTokUnhandledSocialEvent(message);
        }
    }


}
