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

import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.live.GiftsManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TikTokGiftsManager implements GiftsManager {
    private final Map<Integer, Gift> giftsByIdIndex;

    public TikTokGiftsManager(List<Gift> giftList)
    {
        giftsByIdIndex = giftList.stream().collect(Collectors.toConcurrentMap(Gift::getId, e -> e));
    }

    public void attachGift(Gift gift) {
        giftsByIdIndex.put(gift.getId(), gift);
    }

    public void attachGiftsList(List<Gift> gifts) {
        gifts.forEach(this::attachGift);
    }

    public Gift getByName(String name) {
        return getByFilter(e -> e.getName().equalsIgnoreCase(name));
    }

    public Gift getById(int giftId) {
        if (!giftsByIdIndex.containsKey(giftId)) {
            return Gift.UNDEFINED;
        }

        return giftsByIdIndex.get(giftId);
    }

    public Gift getByFilter(Predicate<Gift> filter) {
        return giftsByIdIndex.values()
                .stream()
                .filter(filter)
                .findFirst()
                .orElseGet(() -> Gift.UNDEFINED);
    }

    @Override
    public List<Gift> getManyByFilter(Predicate<Gift> filter) {
        return giftsByIdIndex.values()
                .stream()
                .filter(filter)
                .collect(Collectors.toList());
    }

    public List<Gift> toList() {
        return new ArrayList<Gift>(giftsByIdIndex.values());
    }

    public Map<Integer, Gift> toMap() {
        return Collections.unmodifiableMap(giftsByIdIndex);
    }
}
