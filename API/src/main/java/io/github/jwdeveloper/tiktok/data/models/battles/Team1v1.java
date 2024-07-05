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
package io.github.jwdeveloper.tiktok.data.models.battles;

import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMicBattle;
import lombok.Getter;

import java.util.*;

@Getter
public class Team1v1 extends Team {
    private final int winStreak;
    private final User host;
    private final List<Viewer> viewers;

    public Team1v1(WebcastLinkMicBattle.LinkMicBattleHost hostTeam, WebcastLinkMicBattle msg) {
        long hostId = hostTeam.getId();
        this.winStreak = msg.getTeamDataList().stream().filter(data -> data.getTeamId() == hostId).map(data -> data.getData().getWinStreak()).findFirst().orElse(-1);
        this.totalPoints = msg.getDetailsList().stream().filter(dets -> dets.getId() == hostId).map(dets -> dets.getSummary().getPoints()).findFirst().orElse(-1);
        this.host = new User(hostTeam.getHostGroup(0).getHost(0));
        this.viewers = msg.getViewerTeamList().stream().filter(team -> team.getId() == hostId).findFirst().map(topViewers ->
            topViewers.getViewerGroup(0).getViewerList().stream().map(Viewer::new).toList()).orElseGet(ArrayList::new);
    }
}