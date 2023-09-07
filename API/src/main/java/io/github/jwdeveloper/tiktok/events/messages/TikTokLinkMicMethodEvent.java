package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.messages.LinkMicMethod;
import io.github.jwdeveloper.tiktok.messages.WebcastLinkMicMethod;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokLinkMicMethodEvent extends TikTokHeaderEvent {
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
