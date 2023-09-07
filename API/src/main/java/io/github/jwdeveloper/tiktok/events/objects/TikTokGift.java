package io.github.jwdeveloper.tiktok.events.objects;

import io.github.jwdeveloper.tiktok.messages.WebcastGiftMessage;
import lombok.Getter;
import lombok.Setter;

@Getter
public class TikTokGift {
    private final Gift gift;
    private final User sender;
    @Setter
    private long amount;
    @Setter
    private boolean streakFinished;

    public TikTokGift(WebcastGiftMessage message) {
        gift = new Gift(message.getGift());
        sender = User.MapOrEmpty(message.getUser());
        amount = message.getComboCount();
        streakFinished = message.getRepeatEnd() > 0;
    }
}
