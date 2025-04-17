/*
 * Copyright (c) 2023-2024 jwdeveloper jacekwoln@gmail.com
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

import io.github.jwdeveloper.tiktok.annotations.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.data.models.*;
import io.github.jwdeveloper.tiktok.messages.enums.*;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMicArmies;
import lombok.Getter;

import java.util.*;

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

    private final Map<Long, LinkMicArmy> armies;

    private final BattleType battleType;

    public TikTokLinkMicArmiesEvent(WebcastLinkMicArmies msg) {
        super(msg.getCommon());
        System.out.println(msg);
        battleId = msg.getBattleId();
        armies = new HashMap<>();
        picture = Picture.map(msg.getGifIconImage());
        finished = msg.getTriggerReason() == TriggerReason.TRIGGER_REASON_BATTLE_END;
        battleType = msg.getBattleSettings().getBattleType();

        switch (battleType) {
            case BATTLE_TYPE_NORMAL_BATTLE -> // 1v1 | Fields present - armies
				msg.getArmiesMap().forEach((aLong, userArmies) -> armies.put(aLong, new LinkMicArmy(userArmies)));
            case BATTLE_TYPE_TEAM_BATTLE -> // 2v2 | Fields present - team_armies
				msg.getTeamArmiesList().forEach(teamArmy -> armies.put(teamArmy.getTeamId(), new LinkMicArmy(teamArmy.getUserArmies())));
            case BATTLE_TYPE_INDIVIDUAL_BATTLE -> // 1v1v1 or 1v1v1v1 | Fields present - team_armies
				msg.getTeamArmiesList().forEach(teamArmy -> armies.put(teamArmy.getTeamId(), new LinkMicArmy(teamArmy.getUserArmies())));
            case BATTLE_TYPE_1_V_N -> { // 1 vs Many | Have no data for this yet
                // Most complicated and uncommon battle type - When more data is collected, this will be updated.
            }
        }
    }
}