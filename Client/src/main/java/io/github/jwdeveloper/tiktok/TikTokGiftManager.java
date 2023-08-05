package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.http.TikTokApiService;
import io.github.jwdeveloper.tiktok.live.models.gift.TikTokGift;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class TikTokGiftManager {
    private Logger logger;
    private ClientSettings clientSettings;
    private TikTokApiService apiService;
    private Map<Integer, TikTokGift> gifts;

    public TikTokGiftManager(Logger logger, TikTokApiService apiService, ClientSettings clientSettings) {
        this.logger = logger;
        this.clientSettings = clientSettings;
        this.apiService = apiService;
        this.gifts = new HashMap<>();
    }

    public void loadGifts() {
        if (!clientSettings.isDownloadGiftInfo()) {
            return;
        }
        logger.info("Fetching gifts");
        gifts =apiService.fetchAvailableGifts();
    }

    public List<TikTokGift> getGifts()
    {
        return gifts.values().stream().toList();
    }
}
