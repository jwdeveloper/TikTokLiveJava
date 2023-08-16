package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.events.objects.TikTokGift;
import io.github.jwdeveloper.tiktok.http.TikTokApiService;
import io.github.jwdeveloper.tiktok.models.GiftId;

import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TikTokGiftManager {
    private Logger logger;
    private ClientSettings clientSettings;
    private TikTokApiService apiService;
    private Map<Integer, TikTokGift> gifts;

    @Getter
    private Map<GiftId, TikTokGift> activeGifts;

    public TikTokGiftManager(Logger logger, TikTokApiService apiService, ClientSettings clientSettings) {
        this.logger = logger;
        this.clientSettings = clientSettings;
        this.apiService = apiService;
        this.gifts = new HashMap<>();
        activeGifts = new HashMap<>();
    }

    public void loadGifts() {
        if (!clientSettings.isDownloadGiftInfo()) {
            return;
        }
        logger.info("Fetching gifts");
        //TODO gifts =apiService.fetchAvailableGifts();
    }

    public List<TikTokGift> getGifts()
    {
        return gifts.values().stream().toList();
    }


}
