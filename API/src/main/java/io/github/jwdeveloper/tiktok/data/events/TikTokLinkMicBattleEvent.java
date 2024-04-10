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

import io.github.jwdeveloper.tiktok.annotations.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMicBattle;
import lombok.Getter;

import java.util.*;

/**
 * Triggered every time a battle starts & ends
 */
@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokLinkMicBattleEvent extends TikTokHeaderEvent {
    private final Long battleId;
    private final Integer battleStatus;
    private final List<Team> teams;

    public TikTokLinkMicBattleEvent(WebcastLinkMicBattle msg) {
        super(msg.getCommon());
        battleId = msg.getId();
        battleStatus = msg.getBattleStatus();
        teams = new ArrayList<>();
        if (msg.getHostTeamCount() == 2) { // 1v1 battle
            teams.add(new Team1v1(msg.getHostTeam(0), msg));
            teams.add(new Team1v1(msg.getHostTeam(1), msg));
        } else { // 2v2 battle
            if (isFinished()) {
                teams.add(new Team2v2(msg.getHostData2V2List().stream().filter(data -> data.getTeamNumber() == 1).findFirst().orElse(null), msg));
                teams.add(new Team2v2(msg.getHostData2V2List().stream().filter(data -> data.getTeamNumber() == 2).findFirst().orElse(null), msg));
            } else {
                teams.add(new Team2v2(msg.getHostTeam(0), msg.getHostTeam(1), msg));
                teams.add(new Team2v2(msg.getHostTeam(2), msg.getHostTeam(3), msg));
            }
        }

        // Info:
        // - msg.getDetailsList() & msg.getViewerTeamList() both only have content when battle is finished
        // - msg.getDetailsCount() & msg.getViewerTeamCount() always is 2 only when battle is finished
        // - msg.getHostTeamCount() always is 2 for 1v1 or 4 for 2v2
    }

    /**
     battleStatus of 4 is Ongoing battle & 5 is Finished Battle
     @return true if battle is finished otherwise false
     */
    public boolean isFinished() {
        return battleStatus == 5;
    }

    public abstract static class Team {
        /**
         * Provides a check for verifying if this team represents a 1v1 Team.
         * @return true if this team is of type {@link Team1v1}, false otherwise.
         */
        public boolean is1v1Team() {
            return this instanceof Team1v1;
        }

        /**
         * Provides a check for verifying if this team represents a 1v1 Team.
         * @return true if this team is of type {@link Team1v1}, false otherwise.
         */
        public boolean is2v2Team() {
            return this instanceof Team2v2;
        }

        /**
         * Convenience method to get this team as a {@link Team1v1}. If this team is of some
         * other type, an {@link IllegalStateException} will result. Hence it is best to use this method
         * after ensuring that this element is of the desired type by calling {@link #is1v1Team()} first.
         *
         * @return this team as a {@link Team1v1}.
         * @throws IllegalStateException if this team is of another type.
         */
        public Team1v1 getAs1v1Team() {
            if (is1v1Team())
                return (Team1v1) this;
            throw new IllegalStateException("Not a 1v1Team: " + this);
        }

        /**
         * Convenience method to get this team as a {@link Team2v2}. If this team is of some
         * other type, an {@link IllegalStateException} will result. Hence it is best to use this method
         * after ensuring that this element is of the desired type by calling {@link #is2v2Team()} first.
         *
         * @return this team as a {@link Team2v2}.
         * @throws IllegalStateException if this team is of another type.
         */
        public Team2v2 getAs2v2Team() {
            if (is2v2Team())
                return (Team2v2) this;
            throw new IllegalStateException("Not a 2v2Team: " + this);
        }
    }

    @Getter
    public static class Team1v1 extends Team {
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

    @Getter
    public static class Team2v2 extends Team {
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

    @Getter
    public static class Viewer {
        private final User user;
        private final int points;

        public Viewer(WebcastLinkMicBattle.LinkMicBattleTopViewers.TopViewerGroup.TopViewer topViewer) {
            this.user = new User(topViewer.getId(), null, topViewer.getProfileId(), Picture.map(topViewer.getImages(0)));
            this.points = topViewer.getPoints();
        }
    }
}