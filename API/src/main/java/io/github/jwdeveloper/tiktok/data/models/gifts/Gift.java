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