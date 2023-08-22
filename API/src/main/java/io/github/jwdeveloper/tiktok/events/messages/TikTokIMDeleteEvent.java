package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastImDeleteMessage;
import lombok.Getter;

@Getter
public class TikTokIMDeleteEvent extends TikTokEvent {
    private final byte[] data;

    public TikTokIMDeleteEvent(WebcastImDeleteMessage msg) {
        super(msg.getHeader());
        data = msg.getData().toByteArray();
    }
}
