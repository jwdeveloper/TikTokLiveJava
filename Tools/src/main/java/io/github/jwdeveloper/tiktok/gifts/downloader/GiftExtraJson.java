package io.github.jwdeveloper.tiktok.gifts.downloader;

import com.google.gson.*;
import io.github.jwdeveloper.tiktok.Constants;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.http.TikTokCookieJar;
import io.github.jwdeveloper.tiktok.http.TikTokHttpClient;
import io.github.jwdeveloper.tiktok.http.TikTokHttpRequestFactory;
import io.github.jwdeveloper.tiktok.utils.FilesUtility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GiftExtraJson
{
    public static void main(String[] args) {
       var reuslt =  new GiftExtraJson().run();

       System.out.println(reuslt.size());
    }

    public List<GiftDto> run() {

        var output = new ArrayList<GiftDto>();
        var jsonGifts = getJsonGifts();
        for (var jsonElement : jsonGifts) {
            var gift = getGift(jsonElement);
            output.add(gift);
        }
        return output;
    }


    private GiftDto getGift(JsonElement jsonElement) {

        var id = jsonElement.getAsJsonObject().get("id").getAsInt();
        var name = jsonElement.getAsJsonObject().get("name").getAsString();
        var diamondCost = jsonElement.getAsJsonObject().get("diamondCost").getAsInt();
        var image = jsonElement.getAsJsonObject().get("image").getAsString();
        var gift = new GiftDto();
        gift.setId(id);
        gift.setName(name);
        gift.setDiamondCost(diamondCost);
        gift.setImage(image);
        return gift;
    }

    public static JsonArray getJsonGifts() {

        var extraGifts =FilesUtility.loadFileContent("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Tools\\src\\main\\resources\\gifts\\extra_gifts.json");
        JsonElement jsonElement = JsonParser.parseString(extraGifts);
        return jsonElement.getAsJsonArray();
    }
}
