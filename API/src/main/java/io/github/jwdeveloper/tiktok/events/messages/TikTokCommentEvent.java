package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastChatMessage;
import io.github.jwdeveloper.tiktok.messages.WebcastRoomPinMessage;
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
        super(msg.getCommon());
        user = User.MapOrEmpty(msg.getUser());
        text = msg.getContent();
        language = msg.getContentLanguage();
        mentionedUsers = List.of(User.MapOrEmpty(msg.getAtUser()));
        pictures = msg.getEmotesListList().stream().map(e -> new Picture(e.getEmote().getImage())).toList();
    }
}
