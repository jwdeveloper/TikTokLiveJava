package io.github.jwdeveloper.tiktok.data.models.battles;

import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMicBattle;
import lombok.Getter;

import java.util.*;

@Getter
public class Team1v1 extends Team
{
    /** Value >= 0 when finished otherwise -1 */
    private final int totalPoints;
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