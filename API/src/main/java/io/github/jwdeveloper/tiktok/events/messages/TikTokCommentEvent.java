package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.Nullable;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastChatMessage;
import io.github.jwdeveloper.tiktok.messages.WebcastRoomPinMessage;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class TikTokCommentEvent extends TikTokEvent
{
    @Nullable
    private User user;
    private final String text;
    private final String language;
    private final List<User> mentionedUsers;
    private final List<Picture> pictures;

    public TikTokCommentEvent(WebcastRoomPinMessage.RoomPinMessageData data) {
        super(data.getDetails().getRoomId(), data.getDetails().getMessageId(), data.getDetails().getServerTime());
        if (data.hasSender())
            user = new User(data.getSender());
        text = data.getComment();
        language = data.getLanguage();
        mentionedUsers = new ArrayList<>();
        pictures = new ArrayList<>();
    }

    public TikTokCommentEvent(WebcastChatMessage msg) {
        super(msg.getHeader());
        if (msg.hasSender())
            user = new User(msg.getSender());
        text = msg.getComment();
        language = msg.getLanguage();
        mentionedUsers = msg.getMentionedUsersList().stream().map(User::new).toList();
        pictures = msg.getImagesList().stream().map(e -> new Picture(e.getPicture())).toList();
    }
}
