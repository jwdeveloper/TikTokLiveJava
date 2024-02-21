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

import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftOld;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.live.GiftManager;
import sun.misc.Unsafe;

import java.util.*;
import java.util.logging.Logger;

public class TikTokGiftManager implements GiftManager {

    private final Map<Integer, GiftOld> indexById;
    private final Map<String, GiftOld> indexByName;
    private final Logger logger;

    public TikTokGiftManager(Logger logger)
    {
        indexById = new HashMap<>();
        indexByName = new HashMap<>();
        this.logger = logger;
        init();
    }

    protected void init() {
        for (var gift : GiftOld.values()) {
            indexById.put(gift.getId(), gift);
            indexByName.put(gift.getName(), gift);
        }
    }

    public GiftOld registerGift(int id, String name, int diamondCost, Picture picture) {
        try {
            var constructor = Unsafe.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            var unsafe = (Unsafe) constructor.newInstance();
            GiftOld enumInstance = (GiftOld) unsafe.allocateInstance(GiftOld.class);

            var field = GiftOld.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(enumInstance, id);

            field = GiftOld.class.getDeclaredField("name");
            field.setAccessible(true);
            field.set(enumInstance, name);


         //   EnumSet
            field = GiftOld.class.getDeclaredField("diamondCost");
            field.setAccessible(true);
            field.set(enumInstance, diamondCost);

            field = GiftOld.class.getDeclaredField("picture");
            field.setAccessible(true);
            field.set(enumInstance, picture);

            indexById.put(enumInstance.getId(), enumInstance);
            indexByName.put(enumInstance.getName(), enumInstance);

            return enumInstance;
        } catch (Exception e) {
            throw new TikTokLiveException("Unable to register gift: " + name + ": " + id);
        }
    }

    public GiftOld findById(int giftId) {
        GiftOld gift = indexById.get(giftId);
        return gift == null ? GiftOld.UNDEFINED : gift;
    }

    public GiftOld findByName(String giftName) {
        GiftOld gift = indexByName.get(giftName);
        return gift == null ? GiftOld.UNDEFINED : gift;
    }

    @Override
    public List<GiftOld> getGifts() {
        return indexById.values().stream().toList();
    }
}