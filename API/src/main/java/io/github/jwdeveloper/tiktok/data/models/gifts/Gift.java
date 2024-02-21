package io.github.jwdeveloper.tiktok.data.models.gifts;

import com.google.gson.JsonObject;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import lombok.*;

import java.util.*;

@Data
@AllArgsConstructor
public class Gift
{
    @Getter private static final Set<Gift> gifts = new HashSet<>();
    public static final Gift UNDEFINED = new Gift(-1, "undefined", -1, "", null);

    private final int id;

    private final String name;

    private final int diamondCost;

    private final Picture picture;

    private final JsonObject properties;

    public Gift(int id, String name, int diamondCost, String pictureLink, JsonObject properties) {
        this.id = id;
        this.name = name;
        this.diamondCost = diamondCost;
        this.picture = new Picture(pictureLink);
        this.properties = properties;
    }

    public boolean hasDiamondCostRange(int minimalCost, int maximalCost) {
        return diamondCost >= minimalCost && diamondCost <= maximalCost;
    }

    public boolean hasDiamondCost(int cost) {
        return diamondCost == cost;
    }
}