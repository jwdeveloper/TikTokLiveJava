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
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.data.requests.GiftsData;

import java.util.ArrayList;

public class GiftsDataMapper {
    public GiftsData.Response map(String json) {
        var parsedJson = JsonParser.parseString(json);
        var jsonObject = parsedJson.getAsJsonObject();
        var gifts = jsonObject.entrySet()
                .parallelStream()
                .map(e -> mapSingleGift(e.getValue()))
                .toList();

        return new GiftsData.Response(json, gifts);
    }


    private Gift mapSingleGift(JsonElement jsonElement) {
        var jsonObject = jsonElement.getAsJsonObject();

        var id = jsonObject.get("id").getAsInt();
        var name = jsonObject.get("name").getAsString();
        var diamondCost = jsonObject.get("diamondCost").getAsInt();
        var image =jsonObject.get("image").getAsString();
        return new Gift(id, name, diamondCost, image, jsonObject);
    }
}
