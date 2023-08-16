package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.Nullable;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastEmoteChatMessage;
import lombok.Getter;

@Getter
public class TikTokEmoteEvent extends TikTokEvent
{
    @Nullable
    private User user;
    private final String emoteId;
    private final Picture picture;

    public TikTokEmoteEvent(WebcastEmoteChatMessage msg) {
        super(msg.getHeader());
        if (msg.hasSender()) {
            user = new User(msg.getSender());
        }
        emoteId = msg.getDetails().getId();
        picture = new Picture(msg.getDetails().getImage().getUrl());
    }
}
