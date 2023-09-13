package io.github.jwdeveloper.tiktok.live;

import io.github.jwdeveloper.tiktok.events.objects.TikTokGift;
import io.github.jwdeveloper.tiktok.models.GiftId;
import io.github.jwdeveloper.tiktok.models.gifts.TikTokGiftInfo;

import java.util.Map;

public interface GiftManager {

     /**
      *  Meta information about all TikTok available gifts such as, name, id, description, cost, etc
      *  TikTokGiftInfos are downloaded only if `clientSettings.setDownloadGiftInfo(true);`
      *
      * @return map of metainformations about gitfts where Integer is Gift Id and TikTokGiftInfo is gift data
      * @see TikTokGiftInfo
      */
    Map<Integer, TikTokGiftInfo> getGiftsInfo();


     /**
      *  Active Gifts are updated after TikTokGiftMessageEvent. This map contains gifts that
      *  recently send to host and have active strike
      *
      * @return map of active gifts
      * @see  TikTokGift
      * @see  io.github.jwdeveloper.tiktok.events.messages.TikTokGiftMessageEvent
      */
    Map<GiftId, TikTokGift> getActiveGifts();
}
