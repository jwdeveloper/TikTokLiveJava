package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastRankTextMessage;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokRankTextEvent extends TikTokHeaderEvent {
  private final String eventType;

  private final String label;


  public TikTokRankTextEvent(WebcastRankTextMessage msg) {
    super(0,0,0);//TODO passing info
    eventType = msg.getDetails().getType();
    label =msg.getDetails().getLabel();
  }

}
