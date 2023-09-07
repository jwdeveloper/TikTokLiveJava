package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.Gift;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastGiftMessage;
import lombok.Value;

@Value
@EventMeta(eventType = EventType.Message)
public class TikTokGiftMessageEvent extends TikTokHeaderEvent {

      Gift gift;
      User sender;
      String purchaseId;
      String receipt;
      Long comboCount;
      Boolean streakFinished;
      Long streakIndex;

    public TikTokGiftMessageEvent(WebcastGiftMessage msg) {
        super(msg.getCommon());
        gift = new Gift(msg.getGift());
        sender = User.MapOrEmpty(msg.getUser());
        purchaseId = msg.getLogId();
        receipt = msg.getMonitorExtra();
        comboCount = msg.getComboCount();
        streakFinished = msg.getRepeatEnd() > 0; //todo check values
        streakIndex = msg.getRepeatCount();
    }
}
