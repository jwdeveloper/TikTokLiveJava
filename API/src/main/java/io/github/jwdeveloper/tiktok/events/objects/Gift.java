package io.github.jwdeveloper.tiktok.events.objects;

import lombok.Value;

@Value
public class Gift {
    Long id;
    String name;
    String description;
    Integer diamondCost;
    Integer type;
    Picture picture;

    public Gift(io.github.jwdeveloper.tiktok.messages.GiftStruct gift) {
        id = gift.getId();
        name = gift.getName();
        description = gift.getDescribe();
        diamondCost = gift.getDiamondCount();
        type = gift.getType();
        picture = new Picture(gift.getImage());
    }
}
