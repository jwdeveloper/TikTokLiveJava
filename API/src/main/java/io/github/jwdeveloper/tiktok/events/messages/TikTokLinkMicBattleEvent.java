package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.LinkMicBattleTeam;
import io.github.jwdeveloper.tiktok.messages.WebcastLinkMicBattle;
import lombok.Getter;

import java.util.List;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokLinkMicBattleEvent extends TikTokHeaderEvent {
  private final Long battleId;
  private final List<LinkMicBattleTeam> team1;
  private final List<LinkMicBattleTeam> team2;

  public TikTokLinkMicBattleEvent(WebcastLinkMicBattle msg) {
    super(msg.getHeader());
    battleId = msg.getId();
    team1 = msg.getTeams1List().stream().map(LinkMicBattleTeam::new).toList();
    team2 = msg.getTeams2List().stream().map(LinkMicBattleTeam::new).toList();
  }
}
