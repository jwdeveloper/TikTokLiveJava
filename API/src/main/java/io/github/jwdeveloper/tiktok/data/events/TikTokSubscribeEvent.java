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
package io.github.jwdeveloper.tiktok.data.events;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.models.users.UserAttribute;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastMemberMessage;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastSubNotifyMessage;
import lombok.Getter;


@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokSubscribeEvent extends TikTokHeaderEvent {
    private final User user;


    public TikTokSubscribeEvent(WebcastMemberMessage msg) {
        super(msg.getCommon());
        user = User.map(msg.getUser());
        user.addAttribute(UserAttribute.Subscriber);
    }

    public TikTokSubscribeEvent(WebcastSubNotifyMessage msg) {
        super(msg.getCommon());
        user = User.map(msg.getUser());
        user.addAttribute(UserAttribute.Subscriber);
    }

    public static TikTokSubscribeEvent of(String userName) {
        return new TikTokSubscribeEvent(WebcastMemberMessage.newBuilder()
                .setUser(io.github.jwdeveloper.tiktok.messages.data.User.newBuilder()
                        .setDisplayId(userName)
                        .setNickname(userName)
                        .build())
                .build());
    }
}
