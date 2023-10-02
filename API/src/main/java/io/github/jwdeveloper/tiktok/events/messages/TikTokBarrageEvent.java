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
package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.BarrageData;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastBarrageMessage;
import lombok.Value;

@Value
@EventMeta(eventType = EventType.Message)
public class TikTokBarrageEvent extends TikTokHeaderEvent {
    Picture picture;
    Picture picture2;
    Picture picture3;
    User user;
    BarrageData barrageData;
    public TikTokBarrageEvent(WebcastBarrageMessage msg) {
        super(msg.getHeader());
        picture = Picture.Map(msg.getImage());
        picture2 = Picture.Map(msg.getImage2());
        picture3 = Picture.Map(msg.getImage3());
        user = new User(msg.getUserData().getUser());
        barrageData = new BarrageData(msg.getMessage().getEventType(),
                msg.getMessage().getLabel(),
                msg.getMessage().getData1List().stream().map(e ->
                {
                    var user = new User(e.getUser().getUser());
                    return new BarrageData.BarrageUser(user, e.getData2());
                }).toList()
        );
    }
}
