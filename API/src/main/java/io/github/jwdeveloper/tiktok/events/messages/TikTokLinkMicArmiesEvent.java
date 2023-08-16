package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.LinkMicArmy;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.messages.WebcastLinkMicArmies;
import lombok.Getter;

import java.util.List;

@Getter
public class TikTokLinkMicArmiesEvent extends TikTokEvent {
    private final Long battleId;

    private final Integer battleStatus;

    private final Picture picture;

    private final List<LinkMicArmy> armies;

    public TikTokLinkMicArmiesEvent(WebcastLinkMicArmies msg) {
        super(msg.getHeader());
        battleId = msg.getId();
        armies = msg.getBattleItemsList().stream().map(LinkMicArmy::new).toList();
        picture = new Picture(msg.getPicture());
        battleStatus = msg.getBattleStatus();
    }
}
