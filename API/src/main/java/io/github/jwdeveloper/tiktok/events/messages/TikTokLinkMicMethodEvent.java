package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.LinkMicMethod;
import io.github.jwdeveloper.tiktok.messages.WebcastLinkMicMethod;
import lombok.Getter;

@Getter
public class TikTokLinkMicMethodEvent extends TikTokEvent {
  private final String jSON;

  public TikTokLinkMicMethodEvent(WebcastLinkMicMethod msg) {
    super(msg.getHeader());;
    jSON = "";
  }

  public TikTokLinkMicMethodEvent(LinkMicMethod msg) {
    super(msg.getHeader());;
    jSON = msg.getJson();
  }
}
