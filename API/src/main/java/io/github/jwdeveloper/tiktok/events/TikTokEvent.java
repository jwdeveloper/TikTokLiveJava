package io.github.jwdeveloper.tiktok.events;

import io.github.jwdeveloper.tiktok.messages.*;
import lombok.Getter;

@Getter
public class TikTokEvent {
    private long messageId;
    private long roomId;
    private long timeStamp;

    public TikTokEvent(MessageHeader header) {
        this(header.getMessageId(),header.getRoomId(), header.getTimeStamp1());
    }

    public TikTokEvent(GiftMessageHeader header) {
        this(header.getMessageId(),header.getRoomId(), header.getTimeStamp1());
    }

    public TikTokEvent(MemberMessageHeader header) {
        this(header.getMessageId(),header.getRoomId(), header.getTimeStamp1());
    }

    public TikTokEvent(SocialMessageHeader header) {
        this(header.getMessageId(),header.getRoomId(), header.getTimeStamp1());
    }

    public TikTokEvent(LikeMessageHeader header) {
        this(header.getMessageId(),header.getRoomId(), header.getTimeStamp1());
    }

    public TikTokEvent(long messageId, long roomId, long timeStamp) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.timeStamp = timeStamp;
    }

    public TikTokEvent() {

    }
}
