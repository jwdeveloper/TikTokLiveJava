package io.github.jwdeveloper.tiktok.data.models.gifts;

import com.google.gson.JsonObject;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Gift {
    private final int id;

    private final String name;

    private final int diamondCost;

    private final Picture picture;

    private final JsonObject properties;
}
