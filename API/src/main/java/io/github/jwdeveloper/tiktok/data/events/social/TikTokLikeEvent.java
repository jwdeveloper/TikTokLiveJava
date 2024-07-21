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
package io.github.jwdeveloper.tiktok.data.events.social;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLikeMessage;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastSocialMessage;
import lombok.Getter;



@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokLikeEvent extends TikTokHeaderEvent
{
    private final User user;

    private final int likes;

    private final int totalLikes;

    public TikTokLikeEvent(WebcastSocialMessage msg, int totalLikes) {
        super(msg.getCommon());
        user = User.map(msg.getUser());
        likes = 1;
        this.totalLikes = totalLikes;
    }

    public TikTokLikeEvent(WebcastLikeMessage msg) {
        super(msg.getCommon());
        user = User.map(msg.getUser());
        likes = msg.getCount();
        totalLikes = msg.getTotal();
    }

    public static TikTokLikeEvent of(String userName, int likes)
    {
        return new TikTokLikeEvent(WebcastLikeMessage.newBuilder()
                .setCount(likes)
                .setTotal(likes)
                .setUser(io.github.jwdeveloper.tiktok.messages.data.User.newBuilder()
                        .setDisplayId(userName)
                        .setNickname(userName)
                        .build())
                .build());
    }
}
