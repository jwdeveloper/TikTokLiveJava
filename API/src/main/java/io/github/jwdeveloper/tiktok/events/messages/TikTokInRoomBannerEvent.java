package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastInRoomBannerMessage;
import lombok.Getter;

@Getter
public class TikTokInRoomBannerEvent extends TikTokEvent {
  private String jSON;

  public TikTokInRoomBannerEvent(WebcastInRoomBannerMessage msg) {
    super(msg.getHeader());;
    jSON = msg.getJson();
  }
}
