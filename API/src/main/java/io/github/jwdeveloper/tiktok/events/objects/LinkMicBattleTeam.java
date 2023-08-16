package io.github.jwdeveloper.tiktok.events.objects;


import io.github.jwdeveloper.tiktok.messages.WebcastLinkMicBattle;
import lombok.Getter;

import java.util.List;

@Getter
public class LinkMicBattleTeam {
    private final Long teamId;
    private final List<User> users;

    public LinkMicBattleTeam(WebcastLinkMicBattle.LinkMicBattleTeam team) {
        this.teamId = team.getId();
        this.users = team.getUsersList().stream().map(User::new).toList();
    }
}
