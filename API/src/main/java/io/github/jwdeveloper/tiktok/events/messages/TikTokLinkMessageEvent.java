package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastLinkMessage;
import lombok.Getter;

import java.util.List;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokLinkMessageEvent extends TikTokHeaderEvent {
    private final String token;

    private User user;

    private final List<User> otherUsers;

    public TikTokLinkMessageEvent(WebcastLinkMessage msg) {
        super(msg.getHeader());
        token = msg.getToken();
        if (msg.getUser().getUser().hasUser()) {
            user = new User(msg.getUser().getUser().getUser());
        }
        otherUsers = msg.getUser().getOtherUsersList().stream().map(e -> new User(e.getUser())).toList();
    }
}
