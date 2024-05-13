package io.github.jwdeveloper.tiktok.gifts;

import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.live.GiftsManager;

import java.util.*;
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
        return giftsByIdIndex.getOrDefault(giftId, Gift.UNDEFINED);
    }

    public Gift getByFilter(Predicate<Gift> filter) {
        return giftsByIdIndex.values()
                .stream()
                .filter(filter)
                .findFirst()
                .orElse(Gift.UNDEFINED);
    }

    @Override
    public List<Gift> getManyByFilter(Predicate<Gift> filter) {
        return giftsByIdIndex.values()
                .stream()
                .filter(filter)
                .toList();
    }

    public List<Gift> toList() {
        return giftsByIdIndex.values().stream().toList();
    }

    public Map<Integer, Gift> toMap() {
        return Collections.unmodifiableMap(giftsByIdIndex);
    }
}