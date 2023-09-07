package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastMemberMessage;
import io.github.jwdeveloper.tiktok.messages.WebcastSocialMessage;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Custom)
public class TikTokJoinEvent extends TikTokHeaderEvent {
    private User user;

    private final Long totalViewers;

    public TikTokJoinEvent(WebcastSocialMessage msg) {
        super(msg.getHeader());

        if (msg.hasSender()) {
            user = new User(msg.getSender());
        }

        totalViewers = 0L;
    }

    public TikTokJoinEvent(WebcastMemberMessage msg) {
        super(msg.getHeader());
        if (msg.hasUser()) {
            user = new User(msg.getUser());
        }
        totalViewers = msg.getTotalViewers();
    }
}
