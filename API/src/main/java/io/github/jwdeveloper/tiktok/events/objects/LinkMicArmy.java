package io.github.jwdeveloper.tiktok.events.objects;

import io.github.jwdeveloper.tiktok.messages.LinkMicArmiesItems;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
public class LinkMicArmy {
    private final Long armyId;
    private final List<Army> armies;

    public LinkMicArmy(LinkMicArmiesItems army) {
        armyId = army.getHostUserId();
        armies = army.getBattleGroupsList()
                .stream()
                .map(x -> new Army(x.getUsersList()
                        .stream()
                        .map(User::new).toList(), x.getPoints()))
                .toList();
    }


    @Getter
    @AllArgsConstructor
    public final class Army {
        private final List<User> Users;
        private final Integer Points;
    }
}
