package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.Nullable;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.Gift;
import io.github.jwdeveloper.tiktok.events.objects.TikTokGift;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastGiftMessage;
import lombok.Getter;

@Getter
public class TikTokGiftMessageEvent extends TikTokEvent {

    private final Gift gift;

    @Nullable
    private User sender;

    private final String purchaseId;

    private final String receipt;

    private final Integer amount;

    private final Boolean streakFinished;

    private final Integer streakIndex;

    public TikTokGiftMessageEvent(WebcastGiftMessage msg) {
        super(msg.getHeader());
        gift = new Gift(msg.getGiftDetails());
        if (msg.hasSender()) {
            sender = new User(msg.getSender());
        }
        purchaseId = msg.getLogId();
        receipt = msg.getReceiptJson();
        amount = msg.getAmount();
        streakFinished = msg.getRepeatEnd();
        streakIndex = msg.getRepeatCount();
    }
}
