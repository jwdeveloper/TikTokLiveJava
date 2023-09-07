package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastChatMessage;
import io.github.jwdeveloper.tiktok.messages.WebcastRoomPinMessage;
import lombok.Getter;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
@EventMeta(eventType = EventType.Message)
public class TikTokCommentEvent extends TikTokHeaderEvent {
     User user;
     String text;
     String language;
     List<User> mentionedUsers;
     List<Picture> pictures;

    public TikTokCommentEvent(WebcastRoomPinMessage.RoomPinMessageData data) {
        super(data.getDetails().getRoomId(), data.getDetails().getMessageId(), data.getDetails().getServerTime());
        user = User.MapOrEmpty(data.getSender());
        text = data.getComment();
        language = data.getLanguage();
        mentionedUsers = new ArrayList<>();
        pictures = new ArrayList<>();
    }

    public TikTokCommentEvent(WebcastChatMessage msg) {
        super(msg.getHeader());
        user = User.MapOrEmpty(msg.getSender());
        text = msg.getComment();
        language = msg.getLanguage();
        mentionedUsers = msg.getMentionedUsersList().stream().map(User::new).toList();
        pictures = msg.getImagesList().stream().map(e -> new Picture(e.getImage())).toList();
    }
}
