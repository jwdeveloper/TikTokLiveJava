package io.github.jwdeveloper.tiktok.http.mappers;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import io.github.jwdeveloper.tiktok.data.requests.GiftsData;
import java.util.ArrayList;

public class GiftsDataMapper {
    public GiftsData.Response map(String json) {
        var parsedJson = JsonParser.parseString(json);
        var jsonObject = parsedJson.getAsJsonObject();

        if (!jsonObject.has("data")) {
            return new GiftsData.Response(json, new ArrayList<>());
        }
        var dataElement = jsonObject.getAsJsonObject("data");
        if (!dataElement.has("gifts")) {
            return new GiftsData.Response(json, new ArrayList<>());
        }

        var gifts = dataElement.get("gifts").getAsJsonArray()
                .asList()
                .stream()
                .map(this::mapSingleGift)
                .toList();

        return new GiftsData.Response(json, gifts);
    }


    private GiftsData.GiftModel mapSingleGift(JsonElement jsonElement) {
        var id = jsonElement.getAsJsonObject().get("id").getAsInt();
        var name = jsonElement.getAsJsonObject().get("name").getAsString();
        var diamondCost = jsonElement.getAsJsonObject().get("diamond_count").getAsInt();
        var image = jsonElement.getAsJsonObject()
                .get("image").getAsJsonObject()
                .get("url_list").getAsJsonArray().get(0).getAsString();

        if (image.endsWith(".webp")) {
            image = image.replace(".webp", ".jpg");
        }
        var gift = new GiftsData.GiftModel();
        gift.setId(id);
        gift.setName(name);
        gift.setDiamondCost(diamondCost);
        gift.setImage(image);

        return gift;
    }
}
