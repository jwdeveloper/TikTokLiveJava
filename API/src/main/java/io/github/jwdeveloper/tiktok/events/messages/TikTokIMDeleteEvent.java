package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastImDeleteMessage;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokIMDeleteEvent extends TikTokHeaderEvent {
    private final byte[] data;

    public TikTokIMDeleteEvent(WebcastImDeleteMessage msg) {
        super(msg.getHeader());
        data = msg.getData().toByteArray();
    }
}
