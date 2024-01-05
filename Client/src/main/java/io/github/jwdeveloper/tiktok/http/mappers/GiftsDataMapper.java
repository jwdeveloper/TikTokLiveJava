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
