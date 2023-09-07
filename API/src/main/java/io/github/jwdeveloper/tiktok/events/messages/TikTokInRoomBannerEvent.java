package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastInRoomBannerMessage;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokInRoomBannerEvent extends TikTokHeaderEvent {
  private final String jSON;

  public TikTokInRoomBannerEvent(WebcastInRoomBannerMessage msg) {
    super(msg.getHeader());;
    jSON = msg.getJson();
  }
}
