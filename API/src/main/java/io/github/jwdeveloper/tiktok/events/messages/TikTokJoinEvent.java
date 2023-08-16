package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.Nullable;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastMemberMessage;
import io.github.jwdeveloper.tiktok.messages.WebcastSocialMessage;
import lombok.Getter;

@Getter
public class TikTokJoinEvent extends TikTokEvent {
    @Nullable
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
