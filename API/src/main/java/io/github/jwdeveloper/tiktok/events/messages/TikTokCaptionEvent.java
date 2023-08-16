package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastCaptionMessage;
import lombok.Getter;

@Getter
public class TikTokCaptionEvent extends TikTokEvent {
    private final Long captionTimeStamp;

    private final String iSOLanguage;

    private final String text;

    public TikTokCaptionEvent(WebcastCaptionMessage msg) {
        super(msg.getHeader());
        captionTimeStamp = msg.getTimeStamp();
        iSOLanguage = msg.getCaptionData().getISOLanguage();
        text = msg.getCaptionData().getText();
    }
}
