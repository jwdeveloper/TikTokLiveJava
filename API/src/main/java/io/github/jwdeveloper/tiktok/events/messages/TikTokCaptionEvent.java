package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastCaptionMessage;
import lombok.Value;

@Value
@EventMeta(eventType = EventType.Message)
public class TikTokCaptionEvent extends TikTokHeaderEvent {
    Long captionTimeStamp;

    String iSOLanguage;

    String text;

    public TikTokCaptionEvent(WebcastCaptionMessage msg) {
        super(msg.getHeader());
        captionTimeStamp = msg.getTimeStamp();
        iSOLanguage = msg.getCaptionData().getISOLanguage();
        text = msg.getCaptionData().getText();
    }
}
