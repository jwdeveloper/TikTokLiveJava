package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokUnhandledEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastMemberMessage;
import io.github.jwdeveloper.tiktok.messages.WebcastSocialMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Custom)
public class TikTokUnhandledSocialEvent extends TikTokUnhandledEvent<WebcastSocialMessage>
{
    public TikTokUnhandledSocialEvent(WebcastSocialMessage data) {
        super(data);
    }
}
