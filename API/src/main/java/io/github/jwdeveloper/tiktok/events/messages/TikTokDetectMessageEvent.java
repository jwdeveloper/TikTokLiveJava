package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastMsgDetectMessage;
import lombok.Getter;

import java.util.List;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokDetectMessageEvent extends TikTokHeaderEvent {
   String language;

   List<Number> data;

   List<Number> timings;

  public TikTokDetectMessageEvent(WebcastMsgDetectMessage msg) {
    super(msg.getHeader());;
    language = msg.getLanguage();
    data = List.of(msg.getData2().getData1(), msg.getData2().getData2(), msg.getData2().getData3());
    timings= List.of(msg.getTimestamps().getTimestamp1(), msg.getTimestamps().getTimestamp2(), msg.getTimestamps().getTimestamp3());
  }
}
