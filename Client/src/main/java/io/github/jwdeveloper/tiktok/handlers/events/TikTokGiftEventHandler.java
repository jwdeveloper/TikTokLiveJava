package io.github.jwdeveloper.tiktok.handlers.events;

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftComboEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftSendType;
import io.github.jwdeveloper.tiktok.live.GiftManager;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastGiftMessage;
import lombok.SneakyThrows;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TikTokGiftEventHandler {
    private final GiftManager giftManager;
    private final Map<Long, WebcastGiftMessage> giftsMessages;

    public TikTokGiftEventHandler(GiftManager giftManager) {
        this.giftManager = giftManager;
        giftsMessages = new HashMap<>();
    }

    @SneakyThrows
    public List<TikTokEvent> handleGift(byte[] msg) {
        var currentMessage = WebcastGiftMessage.parseFrom(msg);
        var userId = currentMessage.getUser().getId();
        var currentType = GiftSendType.fromNumber(currentMessage.getSendType());
        var containsPreviousMessage = giftsMessages.containsKey(userId);

        if (!containsPreviousMessage) {
            if (currentType == GiftSendType.Finished) {
                return List.of(getGiftEvent(currentMessage));
            } else {
                giftsMessages.put(userId, currentMessage);
                return List.of(getGiftComboEvent(currentMessage, GiftSendType.Begin));
            }
        }

        var previousMessage = giftsMessages.get(userId);
        var previousType = GiftSendType.fromNumber(previousMessage.getSendType());
        if (currentType == GiftSendType.Active &&
                previousType == GiftSendType.Active) {
            giftsMessages.put(userId, currentMessage);
            return List.of(getGiftComboEvent(currentMessage, GiftSendType.Active));
        }


        if (currentType == GiftSendType.Finished &&
                previousType == GiftSendType.Active) {
            giftsMessages.clear();
            return List.of(
                    getGiftComboEvent(currentMessage, GiftSendType.Finished),
                    getGiftEvent(currentMessage));
        }

        return List.of();
    }

    private TikTokGiftEvent getGiftEvent(WebcastGiftMessage message) {
        var gift = getGiftObject(message);
        return new TikTokGiftEvent(gift, message);
    }

    private TikTokGiftEvent getGiftComboEvent(WebcastGiftMessage message, GiftSendType state) {
        var gift = getGiftObject(message);
        return new TikTokGiftComboEvent(gift, message, state);
    }

    private Gift getGiftObject(WebcastGiftMessage giftMessage) {
        var gift = giftManager.findById((int) giftMessage.getGiftId());
        if (gift == Gift.UNDEFINED) {
            gift = giftManager.findByName(giftMessage.getGift().getName());
        }
        if (gift == Gift.UNDEFINED) {
            gift = giftManager.registerGift(
                    (int) giftMessage.getGift().getId(),
                    giftMessage.getGift().getName(),
                    giftMessage.getGift().getDiamondCount(),
                    Picture.map(giftMessage.getGift().getImage()));
        }
        return gift;
    }
}
