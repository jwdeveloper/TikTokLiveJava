package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastRoomPinMessage;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokRoomPinMessageEvent extends TikTokHeaderEvent {
  private final Long pinTimeStamp;
  private final TikTokCommentEvent comment;

  public TikTokRoomPinMessageEvent(WebcastRoomPinMessage msg) {
    super(msg.getHeader());
    this.pinTimeStamp = msg.getTimestamp();
    this.comment = new TikTokCommentEvent(msg.getPinData1());
  }

}
