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
package io.github.jwdeveloper.tiktok.data.models.battles;

import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.messages.enums.BattleType;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMicBattle;
import lombok.Data;

import java.util.*;

@Data
public class Team {
    /** TeamId used for all battle types */
    private final long teamId;
    /** Value >= 0 when finished otherwise -1 */
    private int totalPoints = -1;
    /** Value >= 0 when battle type is {@link BattleType}.{@code BATTLE_TYPE_NORMAL_BATTLE} otherwise -1 */
    private int winStreak = -1;
    /** Up to N hosts */
    private final List<User> hosts;
    /** Populated when finished */
    private Map<User, Integer> viewers = new HashMap<>();

    // public Team(WebcastLinkMicBattle.BattleUserInfoWrapper anchorInfo, WebcastLinkMicBattle msg) {
    //     long hostId = anchorInfo.getUserId();
    //     this.winStreak = msg.getBattleCombosOrDefault(hostId, WebcastLinkMicBattle.BattleComboInfo.newBuilder().setComboCount(-1).build()).getComboCount();
    //     this.totalPoints = (int) msg.getBattleResultOrDefault(hostId, WebcastLinkMicBattle.BattleResult.newBuilder().setScore(-1).build()).getScore();
    //     this.host = new User(anchorInfo.getUserInfo().getUser());
    //     this.viewers = new HashMap<>();
    //     Optional.ofNullable(msg.getArmiesMap().get(host.getId())).ifPresent(armies ->
    //         armies.getUserArmiesList().forEach(userArmy ->
    //             viewers.put(new User(userArmy), (int) userArmy.getScore())));
    // }
    //
    // public Team(WebcastLinkMicBattle.BattleTeamResult battleTeamResult, WebcastLinkMicBattle msg) {
    //     this.totalPoints = (int) battleTeamResult.getTotalScore();
    //     var host = new User(msg.getAnchorInfoList().stream().filter(data -> data.getUserId() == battleTeamResult.getTeamUsers(0).getUserId()).findFirst().orElseThrow().getUserInfo().getUser());
    //     var cohost = new User(msg.getAnchorInfoList().stream().filter(data -> data.getUserId() == battleTeamResult.getTeamUsers(1).getUserId()).findFirst().orElseThrow().getUserInfo().getUser());
    //     this.hosts = List.of(host, cohost);
    //     this.viewers = new HashMap<>();
    //     Optional.ofNullable(msg.getArmiesMap().get(host.getId())).ifPresent(armies ->
    //         armies.getUserArmiesList().forEach(userArmy ->
    //             viewers.put(new User(userArmy), (int) userArmy.getScore())));
    //     Optional.ofNullable(msg.getArmiesMap().get(cohost.getId())).ifPresent(armies ->
    //         armies.getUserArmiesList().forEach(userArmy ->
    //             viewers.put(new User(userArmy), (int) userArmy.getScore())));
    // }
    //
    public Team(long teamId, List<User> hosts) {
        this.teamId = teamId;
        this.hosts = List.copyOf(hosts);
    }

    public Team(WebcastLinkMicBattle.BattleUserInfo anchorInfo) {
        this.hosts = List.of(new User(anchorInfo.getUser()));
        this.teamId = hosts.get(0).getId();
    }

    public Team(WebcastLinkMicBattle.BattleUserInfo anchorInfo, WebcastLinkMicBattle.BattleComboInfo battleCombo) {
        this(anchorInfo);
        this.winStreak = (int) battleCombo.getComboCount();
    }

    public boolean contains(String name) {
        return hosts.stream().anyMatch(user -> user.getName().equals(name));
    }
}