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
import io.github.jwdeveloper.tiktok.data.models.battles.*;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.messages.enums.LinkMicBattleStatus;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMicBattle;
import lombok.Getter;

/**
 * Triggered every time a battle starts & ends
 */
@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokLinkMicBattleEvent extends TikTokHeaderEvent
{
    private final Long battleId;
    /**
     true if battle is finished otherwise false
     */
    private final boolean finished;
    private final Team team1, team2;

    public TikTokLinkMicBattleEvent(WebcastLinkMicBattle msg) {
        super(msg.getCommon());
        battleId = msg.getId();
        finished = msg.getBattleStatus() == LinkMicBattleStatus.BATTLE_FINISHED;
        if (msg.getHostTeamCount() == 2) { // 1v1 battle
            team1 = new Team1v1(msg.getHostTeam(0), msg);
            team2 = new Team1v1(msg.getHostTeam(1), msg);
        } else { // 2v2 battle
            if (isFinished()) {
                team1 = new Team2v2(msg.getHostData2V2List().stream().filter(data -> data.getTeamNumber() == 1).findFirst().orElse(null), msg);
                team2 = new Team2v2(msg.getHostData2V2List().stream().filter(data -> data.getTeamNumber() == 2).findFirst().orElse(null), msg);
            } else {
                team1 = new Team2v2(msg.getHostTeam(0), msg.getHostTeam(1), msg);
                team2 = new Team2v2(msg.getHostTeam(2), msg.getHostTeam(3), msg);
            }
        }

        // Info:
        // - msg.getDetailsList() & msg.getViewerTeamList() both only have content when battle is finished
        // - msg.getDetailsCount() & msg.getViewerTeamCount() always is 2 only when battle is finished
        // - msg.getHostTeamCount() always is 2 for 1v1 or 4 for 2v2
    }

    /**
     * @param battleHostName name of host to search
     * @return Team1v1 instance containing name of host or null if no team found */
    public Team1v1 get1v1Team(String battleHostName) {
        if (!is1v1())
            throw new TikTokLiveException("Teams are not instance of 1v1 battle!");
        if (team1.getAs1v1Team().getHost().getName().equals(battleHostName))
            return team1.getAs1v1Team();
        if (team2.getAs1v1Team().getHost().getName().equals(battleHostName))
            return team2.getAs1v1Team();
        return null;
    }

    public Team2v2 get2v2Team(String battleHostName) {
        if (!is2v2())
            throw new TikTokLiveException("Teams are not instance of 2v2 battle!");
        if (team1.getAs2v2Team().getHosts().stream().anyMatch(user -> user.getName().equals(battleHostName)))
            return team1.getAs2v2Team();
        if (team2.getAs2v2Team().getHosts().stream().anyMatch(user -> user.getName().equals(battleHostName)))
            return team2.getAs2v2Team();
        return null;
    }

    public boolean is1v1() {
        return team1.is1v1Team() || team2.is1v1Team();
    }

    public boolean is2v2() {
        return team1.is2v2Team() || team2.is2v2Team();
    }

    public boolean isTie() {
        return isFinished() && team1.getTotalPoints() == team2.getTotalPoints();
    }
}