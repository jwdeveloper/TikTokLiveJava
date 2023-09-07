package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastLikeMessage;
import io.github.jwdeveloper.tiktok.messages.WebcastSocialMessage;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Custom)
public class TikTokLikeEvent extends TikTokHeaderEvent
{
    private User sender;

    private final Integer count;

    private final Long totalLikes;

    public TikTokLikeEvent(WebcastSocialMessage msg) {
        super(msg.getHeader());
        if (msg.hasSender()) {
            sender = new User(msg.getSender());
        }
        count = 1;
        totalLikes = 0L;
    }

    public TikTokLikeEvent(WebcastLikeMessage msg) {
        super(msg.getHeader());

        if (msg.hasSender()) {
            sender = new User(msg.getSender());
        }

        count = msg.getCount();
        totalLikes = msg.getTotalLikes();
    }
}
