package io.github.jwdeveloper.tiktok.live;

import io.github.jwdeveloper.tiktok.events.objects.TikTokGift;
import io.github.jwdeveloper.tiktok.models.GiftId;
import io.github.jwdeveloper.tiktok.models.gifts.TikTokGiftInfo;

import java.util.Map;

public interface GiftManager
{
     Map<Integer, TikTokGiftInfo> getGiftsInfo();

     Map<GiftId, TikTokGift> getActiveGifts();
}
