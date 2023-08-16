package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastRankTextMessage;
import lombok.Getter;

@Getter
public class TikTokRankTextEvent extends TikTokEvent {
  private String eventType;

  private String label;


  public TikTokRankTextEvent(WebcastRankTextMessage msg) {
    super(0,0,0);//TODO passing info
    eventType = msg.getDetails().getType();
    label =msg.getDetails().getLabel();
  }

}
