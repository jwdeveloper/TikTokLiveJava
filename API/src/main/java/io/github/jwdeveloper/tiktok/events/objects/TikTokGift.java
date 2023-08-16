package io.github.jwdeveloper.tiktok.events.objects;

import io.github.jwdeveloper.tiktok.messages.WebcastGiftMessage;
import lombok.Getter;
import lombok.Setter;

@Getter
public class TikTokGift {
    private final Gift gift;
    private User sender;
    @Setter
    private int amount;

    @Setter
    private  boolean streakFinished;

    public TikTokGift(WebcastGiftMessage message) {
        gift = new Gift(message.getGiftDetails());
        if (message.hasSender()) {
            sender = new User(message.getSender());
        }
        amount = message.getAmount();
        streakFinished = message.getRepeatEnd();
    }
}
