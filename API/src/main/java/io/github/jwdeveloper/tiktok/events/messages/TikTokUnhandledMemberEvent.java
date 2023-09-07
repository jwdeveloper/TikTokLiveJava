package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokUnhandledEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastMemberMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokUnhandledMemberEvent extends TikTokUnhandledEvent<WebcastMemberMessage>
{
    public TikTokUnhandledMemberEvent(WebcastMemberMessage data) {
        super(data);
    }
}
