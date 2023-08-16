package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.Nullable;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastLinkMessage;
import lombok.Getter;

import java.util.List;

@Getter
public class TikTokLinkMessageEvent extends TikTokEvent {
    private final String token;

    @Nullable
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
