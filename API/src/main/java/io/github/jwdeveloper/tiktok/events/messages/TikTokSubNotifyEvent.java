package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastSubNotifyMessage;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokSubNotifyEvent extends TikTokHeaderEvent {
    private User user;

    public TikTokSubNotifyEvent(WebcastSubNotifyMessage msg) {
        super(msg.getHeader());

        if (msg.hasSender()) {
            user = new User(msg.getSender());
        }

    }

}
