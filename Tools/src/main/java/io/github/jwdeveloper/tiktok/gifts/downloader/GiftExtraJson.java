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

import com.google.gson.*;
import io.github.jwdeveloper.tiktok.utils.FilesUtility;

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
