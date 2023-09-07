package io.github.jwdeveloper.tiktok.events.objects;


import io.github.jwdeveloper.tiktok.messages.WebcastLinkMicBattle;
import lombok.Getter;
import lombok.Value;

import java.util.List;

@Value
public class LinkMicBattleTeam {
    Long teamId;
    List<User> users;

    public LinkMicBattleTeam(WebcastLinkMicBattle.LinkMicBattleTeam team) {
        this.teamId = team.getId();
        this.users = team.getUsersList().stream().map(User::new).toList();
    }
}
