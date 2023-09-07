package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.RoomMessage;
import io.github.jwdeveloper.tiktok.messages.SystemMessage;
import io.github.jwdeveloper.tiktok.messages.WebcastLiveIntroMessage;
import io.github.jwdeveloper.tiktok.messages.WebcastRoomMessage;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokRoomMessageEvent extends TikTokHeaderEvent {
    private User host;
    private String hostLanguage;
    private final String message;

    public TikTokRoomMessageEvent(WebcastRoomMessage msg) {
        super(msg.getHeader());
        message = msg.getData();
    }

    public TikTokRoomMessageEvent(SystemMessage msg) {
        super(msg.getHeader());
        message = msg.getMessage();
    }

    public TikTokRoomMessageEvent(RoomMessage msg) {
        super(msg.getHeader());
        message = msg.getMessage();
    }

    public TikTokRoomMessageEvent(WebcastLiveIntroMessage msg) {
        super(msg.getHeader());
        if (msg.hasHost()) {
            host = new User(msg.getHost());
        }
        message = msg.getDescription();
        hostLanguage = msg.getLanguage();
    }

}
