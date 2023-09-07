package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokUnhandledEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastControlMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Custom)
public class TikTokUnhandledControlEvent extends TikTokUnhandledEvent<WebcastControlMessage> {

    public TikTokUnhandledControlEvent(WebcastControlMessage data) {
        super(data);
    }
}
