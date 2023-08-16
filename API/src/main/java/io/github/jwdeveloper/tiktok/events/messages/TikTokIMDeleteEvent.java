package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastImDeleteMessage;
import lombok.Getter;

@Getter
public class TikTokIMDeleteEvent extends TikTokEvent {
  private final String data1;
  private final String data2;

  public TikTokIMDeleteEvent(WebcastImDeleteMessage msg) {
    super(msg.getHeader());;
    data1 = msg.getData1();
    data2 = msg.getData2();
  }
}
