package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastLinkMicFanTicketMethod;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokLinkMicFanTicketEvent extends TikTokHeaderEvent {
    private final Long id;
    private final Integer data1;
    private final Integer data2;

    public TikTokLinkMicFanTicketEvent(WebcastLinkMicFanTicketMethod msg) {
        super(msg.getHeader());
        id = msg.getData().getDetails().getId();
        data1 = msg.getData().getData1();
        data2 = msg.getData().getDetails().getData();
    }
}
