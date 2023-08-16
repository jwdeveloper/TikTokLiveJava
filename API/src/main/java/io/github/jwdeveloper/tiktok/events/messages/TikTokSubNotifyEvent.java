package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastSubNotifyMessage;
import lombok.Getter;

@Getter
public class TikTokSubNotifyEvent extends TikTokEvent {
    private User user;

    public TikTokSubNotifyEvent(WebcastSubNotifyMessage msg) {
        super(msg.getHeader());

        if (msg.hasSender()) {
            user = new User(msg.getSender());
        }

    }

}
