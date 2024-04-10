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
import io.github.jwdeveloper.tiktok.data.models.LinkMicArmy;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.messages.enums.LinkMicBattleStatus;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMicArmies;
import lombok.Getter;

import java.util.List;

/**
 * Triggered every time a battle participant receives points. Contains the current status of the battle and the army that suported the group.
 */
@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokLinkMicArmiesEvent extends TikTokHeaderEvent {
    private final Long battleId;
    /**
     true if battle is finished otherwise false
     */
    private final boolean finished;

    private final Picture picture;

    private final List<LinkMicArmy> armies;

    public TikTokLinkMicArmiesEvent(WebcastLinkMicArmies msg) {
        super(msg.getCommon());
        battleId = msg.getId();
        armies = msg.getBattleItemsList().stream().map(LinkMicArmy::new).toList();
        picture = Picture.map(msg.getImage());
        finished = msg.getBattleStatus() == LinkMicBattleStatus.ARMY_FINISHED;
    }
}