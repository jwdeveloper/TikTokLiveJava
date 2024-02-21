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
package io.github.jwdeveloper.tiktok.gifts;

import com.google.gson.JsonObject;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.live.GiftManager;

import java.util.*;
import java.util.logging.Logger;

public class TikTokGiftManager implements GiftManager {

    private final Map<Integer, Gift> indexById;
    private final Map<String, Gift> indexByName;
    private final Logger logger;

    public TikTokGiftManager(Logger logger)
    {
        indexById = new HashMap<>();
        indexByName = new HashMap<>();
        this.logger = logger;
        init();
    }

    protected void init() {
        for (var gift : Gift.getGifts()) {
            indexById.put(gift.getId(), gift);
            indexByName.put(gift.getName(), gift);
        }
    }

    public Gift registerGift(int id, String name, int diamondCost, Picture picture, JsonObject properties) {
        Gift gift = new Gift(id, name, diamondCost, picture, properties);
        indexById.put(gift.getId(), gift);
        indexByName.put(gift.getName(), gift);
        return gift;
    }

    public Gift findById(int giftId) {
        return indexById.getOrDefault(giftId, Gift.UNDEFINED);
    }

    public Gift findByName(String giftName) {
        return indexByName.getOrDefault(giftName, Gift.UNDEFINED);
    }

    @Override
    public List<Gift> getGifts() {
        return indexById.values().stream().toList();
    }
}