package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.events.objects.TikTokGift;
import io.github.jwdeveloper.tiktok.live.GiftManager;
import io.github.jwdeveloper.tiktok.messages.WebcastGiftMessage;
import io.github.jwdeveloper.tiktok.models.GiftId;
import io.github.jwdeveloper.tiktok.models.gifts.TikTokGiftInfo;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class TikTokGiftManager implements GiftManager {

    @Getter
    private final Map<Integer, TikTokGiftInfo> giftsInfo;

    @Getter
    private final Map<GiftId, TikTokGift> activeGifts;

    public TikTokGiftManager() {
        giftsInfo = new HashMap<>();
        activeGifts = new HashMap<>();
    }

    public TikTokGift updateActiveGift(WebcastGiftMessage giftMessage) {
        var giftId = new GiftId(giftMessage.getGiftId(), giftMessage.getUser().getIdStr());
        if (activeGifts.containsKey(giftId)) {
            var gift = activeGifts.get(giftId);
            gift.setAmount(giftMessage.getComboCount());
        } else {
            var newGift = new TikTokGift(giftMessage);
            activeGifts.put(giftId, newGift);
        }

        var gift = activeGifts.get(giftId);

        if (giftMessage.getRepeatEnd() > 0)
        {
            gift.setStreakFinished(true);
            activeGifts.remove(giftId);
        }
        return gift;
    }

    public void loadGifsInfo(Map<Integer, TikTokGiftInfo> gifts) {
        this.giftsInfo.putAll(gifts);
    }
}
