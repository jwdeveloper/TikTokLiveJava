package io.github.jwdeveloper.tiktok.events.objects;

import lombok.Getter;

@Getter
public class Gift {
    private final Long id;

    private final String name;

    private final String description;

    private final Integer diamondCost;

    private final Integer type;

    private final Picture picture;

    public Gift(io.github.jwdeveloper.tiktok.messages.Gift gift) {
        id = gift.getId();
        name = gift.getName();
        description = gift.getDescription();
        diamondCost = gift.getCoinCount();
        type = gift.getGiftType();
        picture = new Picture(gift.getImage());
    }
}
