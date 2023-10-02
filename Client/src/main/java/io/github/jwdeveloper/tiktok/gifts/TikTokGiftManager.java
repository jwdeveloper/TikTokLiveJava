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

import io.github.jwdeveloper.tiktok.events.objects.Gift;
import io.github.jwdeveloper.tiktok.events.objects.TikTokGift;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.live.GiftManager;
import io.github.jwdeveloper.tiktok.messages.WebcastGiftMessage;
import io.github.jwdeveloper.tiktok.models.GiftId;
import lombok.Getter;
import sun.misc.Unsafe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TikTokGiftManager implements GiftManager {

    @Getter
    private final Map<GiftId, TikTokGift> activeGifts;
    private final Map<Integer, Gift> indexById;
    private final Map<String, Gift> indexByName;

    public TikTokGiftManager() {
        activeGifts = new HashMap<>();
        indexById = new HashMap<>();
        indexByName = new HashMap<>();
        init();
    }

    protected void init() {
        for (var gift : Gift.values()) {
            indexById.put(gift.getId(), gift);
            indexByName.put(gift.getName(), gift);
        }
    }

    public TikTokGift updateActiveGift(WebcastGiftMessage giftMessage) {
        var giftId = new GiftId(giftMessage.getGiftId(), giftMessage.getUser().getIdStr());
        if (activeGifts.containsKey(giftId)) {
            var gift = activeGifts.get(giftId);
            gift.setAmount(giftMessage.getComboCount());
        } else {
            var newGift = new TikTokGift(findById((int) giftMessage.getGiftId()), giftMessage);
            activeGifts.put(giftId, newGift);
        }

        var gift = activeGifts.get(giftId);

        if (giftMessage.getRepeatEnd() > 0) {
            gift.setStreakFinished(true);
            activeGifts.remove(giftId);
        }
        return gift;
    }

    public Gift registerGift(int id, String name, int diamondCost) {
        try {
            var constructor = Unsafe.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            var unsafe = (Unsafe) constructor.newInstance();
            Gift enumInstance = (Gift) unsafe.allocateInstance(Gift.class);

            var field = Gift.class.getDeclaredField("id");
            field.setAccessible(true);
            field.set(enumInstance, id);

            field = Gift.class.getDeclaredField("name");
            field.setAccessible(true);
            field.set(enumInstance, name);


            field = Gift.class.getDeclaredField("diamondCost");
            field.setAccessible(true);
            field.set(enumInstance, diamondCost);

            indexById.put(enumInstance.getId(), enumInstance);
            indexByName.put(enumInstance.getName(), enumInstance);

            return enumInstance;
        } catch (Exception e) {
            throw new TikTokLiveException("Unable to register gift: " + name + ": " + id);
        }
    }

    public Gift findById(int giftId) {
        if (!indexById.containsKey(giftId)) {
            return Gift.UNDEFINED;
        }
        return indexById.get(giftId);
    }

    public Gift findByName(String giftName) {
        if (!indexByName.containsKey(giftName)) {
            return Gift.UNDEFINED;
        }
        return indexByName.get(giftName);
    }

    @Override
    public List<Gift> getGifts()
    {
        return indexById.values().stream().toList();
    }

}
