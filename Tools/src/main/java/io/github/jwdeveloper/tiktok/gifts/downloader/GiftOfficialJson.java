/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.gifts.downloader;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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

public class GiftOfficialJson {

    public static void main(String[] args) {
        new GiftOfficialJson().run();
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
        var diamondCost = jsonElement.getAsJsonObject().get("diamond_count").getAsInt();
        var image = jsonElement.getAsJsonObject()
                .get("image").getAsJsonObject()
                .get("url_list").getAsJsonArray().get(0).getAsString();

        if(image.endsWith(".webp"))
        {
            image = image.replace(".webp",".jpg");
        }
        var gift = new GiftDto();
        gift.setId(id);
        gift.setName(name);
        gift.setDiamondCost(diamondCost);
        gift.setImage(image);
        return gift;
    }

    public static JsonArray getJsonGifts() {
        var jar = new TikTokCookieJar();
        var tiktokHttpClient = new TikTokHttpClient(jar, new TikTokHttpRequestFactory(jar));
        var settings = Constants.DefaultClientSettings();


        var dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        var now = LocalDateTime.now();
        var date = now.format(dtf).replace("/", "_");
        var fileName = "official_" + date + ".json";

        try {
            var response = tiktokHttpClient.getJObjectFromWebcastAPI("gift/list/", settings.getClientParameters());
            var gson = new GsonBuilder().setPrettyPrinting().create();
            FilesUtility.saveFile("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Tools\\src\\main\\resources\\gifts\\official\\" + fileName, gson.toJson(response));
            if (!response.has("data")) {
                return new JsonArray();
            }
            var dataJson = response.getAsJsonObject("data");
            if (!dataJson.has("gifts")) {
                return new JsonArray();
            }
            return dataJson.get("gifts").getAsJsonArray();
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Failed to fetch giftTokens from WebCast, see stacktrace for more info.", e);
        }
    }
}
