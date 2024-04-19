package io.github.jwdeveloper.tiktok.data.models.gifts;

import com.google.gson.JsonObject;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import lombok.Data;

@Data
public class Gift {
    public static final Gift UNDEFINED = new Gift(-1, "undefined", -1, "");

    private final int id;

    private final String name;

    private final int diamondCost;

    private Picture picture;

    private final JsonObject properties;

    public Gift(int id, String name, int diamondCost, Picture pictureLink, JsonObject properties) {
        this.id = id;
        this.name = name;
        this.diamondCost = diamondCost;
        this.picture = pictureLink;
        this.properties = properties;
    }


    public Gift(int id, String name, int diamondCost, String pictureLink) {
        this(id, name, diamondCost, new Picture(pictureLink), new JsonObject());
    }

    public Gift(int id, String name, int diamondCost, Picture picture) {
        this(id, name, diamondCost, picture, new JsonObject());
    }

    public boolean hasDiamondCostRange(int minimalCost, int maximalCost) {
        return diamondCost >= minimalCost && diamondCost <= maximalCost;
    }

    public boolean hasDiamondCost(int cost) {
        return diamondCost == cost;
    }
}