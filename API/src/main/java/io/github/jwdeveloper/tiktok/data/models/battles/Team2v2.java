package io.github.jwdeveloper.tiktok.data.models.battles;

import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMicBattle;
import lombok.Getter;

import java.util.*;

@Getter
public class Team2v2 extends Team {
    /** Value >= 0 when finished otherwise -1 */
    private final int totalPoints;

    private final List<User> hosts;
    private final List<Viewer> viewers;

    public Team2v2(WebcastLinkMicBattle.LinkMicBattleHost hostTeam1, WebcastLinkMicBattle.LinkMicBattleHost hostTeam2, WebcastLinkMicBattle msg) {
        this.totalPoints = -1;
        this.hosts = List.of(new User(hostTeam1.getHostGroup(0).getHost(0)), new User(hostTeam2.getHostGroup(0).getHost(0)));
        this.viewers = new ArrayList<>();
    }

    public Team2v2(WebcastLinkMicBattle.Host2v2Data hd, WebcastLinkMicBattle msg) {
        this.totalPoints = hd.getTotalPoints();
        var host = new User(msg.getHostTeamList().stream().filter(data -> data.getId() == hd.getHostdata(0).getHostId()).findFirst().orElseThrow().getHostGroup(0).getHost(0));
        var cohost = new User(msg.getHostTeamList().stream().filter(data -> data.getId() == hd.getHostdata(1).getHostId()).findFirst().orElseThrow().getHostGroup(0).getHost(0));
        this.hosts = List.of(host, cohost);
        this.viewers = msg.getViewerTeamList().stream().filter(team -> team.getId() == host.getId() || team.getId() == cohost.getId()).findFirst().map(topViewers ->
            topViewers.getViewerGroup(0).getViewerList().stream().map(Viewer::new).toList()).orElseGet(ArrayList::new);
    }
}