package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastEmoteChatMessage;
import lombok.Value;

@Value
@EventMeta(eventType = EventType.Message)
public class TikTokEmoteEvent extends TikTokHeaderEvent {
    User user;
    String emoteId;
    Picture picture;

    public TikTokEmoteEvent(WebcastEmoteChatMessage msg) {
        super(msg.getHeader());
        user = User.MapOrEmpty(msg.getSender());
        emoteId = msg.getDetails().getId();
        picture = new Picture(msg.getDetails().getImage().getUrl());
    }
}
