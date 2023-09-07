package io.github.jwdeveloper.tiktok.events.objects;

import io.github.jwdeveloper.tiktok.messages.LinkMicArmiesItems;
import lombok.Value;

import java.util.List;

@Value
public class LinkMicArmy {
    Long armyId;
    List<Army> armies;

    public LinkMicArmy(LinkMicArmiesItems army) {
        armyId = army.getHostUserId();
        armies = army.getBattleGroupsList()
                .stream()
                .map(x -> new Army(x.getUsersList().stream().map(User::MapOrEmpty).toList(), x.getPoints()))


                .toList();
    }

    @Value
    public static class Army {
        List<User> Users;
        Integer Points;
    }
}
