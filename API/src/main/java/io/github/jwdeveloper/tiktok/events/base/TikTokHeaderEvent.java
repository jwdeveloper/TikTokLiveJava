package io.github.jwdeveloper.tiktok.events.base;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.*;
import lombok.Getter;

@Getter
public class TikTokHeaderEvent extends TikTokEvent {
    private final long messageId;
    private final long roomId;
    private final long timeStamp;

    public TikTokHeaderEvent(Common header) {
        this(header.getMsgId(), header.getRoomId(), header.getCreateTime());
    }

    public TikTokHeaderEvent(MessageHeader header) {
        this(header.getMessageId(), header.getRoomId(), header.getTimeStamp1());
    }

    public TikTokHeaderEvent(GiftMessageHeader header) {
        this(header.getMessageId(), header.getRoomId(), header.getTimeStamp1());
    }

    public TikTokHeaderEvent(MemberMessageHeader header) {
        this(header.getMessageId(), header.getRoomId(), header.getTimeStamp1());
    }

    public TikTokHeaderEvent(SocialMessageHeader header) {
        this(header.getMessageId(), header.getRoomId(), header.getTimeStamp1());
    }

    public TikTokHeaderEvent(LikeMessageHeader header) {
        this(header.getMessageId(), header.getRoomId(), header.getTimeStamp1());
    }

    public TikTokHeaderEvent(long messageId, long roomId, long timeStamp) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.timeStamp = timeStamp;
    }

    public TikTokHeaderEvent() {
        messageId = 0;
        roomId = 0;
        timeStamp = 0;
    }
}
