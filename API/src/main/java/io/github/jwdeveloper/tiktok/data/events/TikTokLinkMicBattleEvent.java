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
import io.github.jwdeveloper.tiktok.data.models.battles.Team;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.messages.data.*;
import io.github.jwdeveloper.tiktok.messages.enums.*;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMicBattle;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

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
    private final List<Team> teams;
    private final BattleType battleType;

    public TikTokLinkMicBattleEvent(WebcastLinkMicBattle msg) {
        super(msg.getCommon());
        System.out.println(msg);
        battleId = msg.getBattleId();
        finished = msg.getAction() == BattleAction.BATTLE_ACTION_FINISH;
        battleType = msg.getBattleSetting().getBattleType();
        teams = new ArrayList<>();
        switch (battleType) {
            case BATTLE_TYPE_NORMAL_BATTLE -> { // 1v1 | Fields present - anchor_info, battle_combos
                for (Long userId : msg.getAnchorInfoMap().keySet())
					teams.add(new Team(msg.getAnchorInfoOrThrow(userId), msg.getBattleCombosOrThrow(userId)));
                if (finished) { // Additional fields present - battle_result, armies
                    for (Team team : teams) {
                        Long userId = team.getHosts().get(0).getId();
                        team.setTotalPoints((int) msg.getBattleResultOrThrow(userId).getScore());
                        team.setViewers(msg.getArmiesOrThrow(userId).getUserArmyList().stream().collect(Collectors.toMap(User::new, bua -> (int) bua.getScore())));
                    }
                }
            }
            case BATTLE_TYPE_TEAM_BATTLE -> { // 2v2 | Fields present - anchor_info
                if (finished) { // Additional fields present - team_battle_result, team_armies
                    for (BattleTeamUserArmies army : msg.getTeamArmiesList()) {
                        Team team = new Team(army.getTeamId(), army.getTeamUsersList().stream()
                            .map(BattleTeamUser::getUserId).map(userId -> new User(msg.getAnchorInfoOrThrow(userId).getUser())).toList());
                        team.setTotalPoints((int) army.getTeamTotalScore());
                        team.setViewers(army.getUserArmies().getUserArmyList().stream().collect(Collectors.toMap(User::new, bua -> (int) bua.getScore())));
                        teams.add(team);
                    }
                } else { // Additional fields present - team_users
                    for (WebcastLinkMicBattle.TeamUsersInfo teamUsersInfo : msg.getTeamUsersList())
						teams.add(new Team(teamUsersInfo.getTeamId(), teamUsersInfo.getUserIdsList().stream()
                            .map(userId -> new User(msg.getAnchorInfoOrThrow(userId).getUser())).toList()));
                }
            }
            case BATTLE_TYPE_INDIVIDUAL_BATTLE -> { // 1v1v1 or 1v1v1v1 | Fields present - anchor_info
                teams.addAll(msg.getAnchorInfoMap().values().stream().map(Team::new).toList());
                if (finished) { // Additional fields present - team_battle_result, team_armies
                    for (Team team : teams) {
                        Long userId = team.getHosts().get(0).getId();
						BattleTeamUserArmies army = msg.getTeamArmiesList().stream().filter(btua -> btua.getTeamId() == userId).findFirst().orElseThrow();
                        team.setTotalPoints((int) army.getTeamTotalScore());
                        team.setViewers(army.getUserArmies().getUserArmyList().stream().collect(Collectors.toMap(User::new, bua -> (int) bua.getScore())));
                    }
                }
            }
            case BATTLE_TYPE_1_V_N -> { // 1 vs Many | Have no data for this yet
                // Most complicated and uncommon battle type - When more data is collected, this will be updated.
            }
        }
    }

    /** 1 host vs 1 host */
    public boolean is1v1() {
        return battleType == BattleType.BATTLE_TYPE_NORMAL_BATTLE;
    }

    /** 2 hosts vs 2 hosts*/
    public boolean is2v2() {
        return battleType == BattleType.BATTLE_TYPE_TEAM_BATTLE;
    }

    /** Up to four users battling each other all on separate teams */
    public boolean isIndividual() {
        return battleType == BattleType.BATTLE_TYPE_INDIVIDUAL_BATTLE;
    }

    /** 1 host vs N hosts | N max value unknown */
    public boolean isMultiTeam() {
        return battleType == BattleType.BATTLE_TYPE_1_V_N;
    }

    public boolean isTie() {
        return isFinished() && isTeamsTie();
    }

    private boolean isTeamsTie() {
        int referencePoints = teams.get(0).getTotalPoints();
        return teams.stream().allMatch(team -> team.getTotalPoints() == referencePoints);
    }
}