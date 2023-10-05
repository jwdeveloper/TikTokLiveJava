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
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.events.objects.barrage.BarrageParam;
import io.github.jwdeveloper.tiktok.events.objects.barrage.FansLevelParam;
import io.github.jwdeveloper.tiktok.events.objects.barrage.SubscribeGiftParam;
import io.github.jwdeveloper.tiktok.events.objects.barrage.UserGradeParam;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastBarrageMessage;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokBarrageEvent extends TikTokHeaderEvent {
    private final Picture icon;
    private final Picture backGround;
    private final Picture rightIcon;
    private final String eventName;
    private final int duration;
    private BarrageParam barrageParam;

    public TikTokBarrageEvent(WebcastBarrageMessage msg) {
        super(msg.getCommon());
        icon = Picture.Map(msg.getIcon());
        eventName = msg.getEvent().getEventName();
        backGround = Picture.Map(msg.getBackground());
        rightIcon = Picture.Map(msg.getRightIcon());
        duration = msg.getDuration();
        switch (msg.getMsgType()) {
            case GRADEUSERENTRANCENOTIFICATION:
                barrageParam = new UserGradeParam(msg.getUserGradeParam());
            case FANSLEVELUPGRADE:
                barrageParam = new FansLevelParam(msg.getFansLevelParam());
            case SUBSCRIBEGIFT:
                barrageParam = new SubscribeGiftParam(msg.getSubscribeGiftParam());
            default:
                barrageParam = new BarrageParam();
        }
    }
}
