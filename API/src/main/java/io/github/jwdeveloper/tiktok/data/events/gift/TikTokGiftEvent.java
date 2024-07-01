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
package io.github.jwdeveloper.tiktok.data.events.gift;

import io.github.jwdeveloper.tiktok.annotations.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.gifts.*;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastGiftMessage;
import lombok.Getter;

@EventMeta(eventType = EventType.Message)
@Getter
public class TikTokGiftEvent extends TikTokHeaderEvent {
    private final Gift gift;
    private final User user;
    private final User toUser;
    private final int combo;

    public TikTokGiftEvent(Gift gift, User liveHost, WebcastGiftMessage msg) {
        super(msg.getCommon());
        this.gift = gift;
        user = User.map(msg.getUser(), msg.getUserIdentity());
        if (msg.getToUser().getNickname().isEmpty()) {
            toUser = liveHost;
        } else {
            toUser = User.map(msg.getToUser());
        }
        combo = msg.getComboCount();
    }

    public TikTokGiftEvent(Gift gift) {
        this.gift = gift;
        user = new User(0L, "sender", new Picture(""));
        toUser = new User(0L, "receiver", new Picture(""));
        combo = 1;
    }


    public static TikTokGiftEvent of(Gift gift) {
        return new TikTokGiftEvent(gift);
    }

    public static TikTokGiftEvent of(String name, int id, int diamonds) {
        return TikTokGiftEvent.of(new Gift(id, name, diamonds, ""));
    }
}