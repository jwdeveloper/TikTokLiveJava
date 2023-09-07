package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastEnvelopeMessage;
import lombok.Getter;
import lombok.Value;

@Value
@EventMeta(eventType = EventType.Message)
public class TikTokEnvelopeEvent extends TikTokHeaderEvent {
  User user;
  public TikTokEnvelopeEvent(WebcastEnvelopeMessage msg) {
    super(msg.getHeader());
    user = new User(msg.getUser().getId(), msg.getUser().getUsername());
  }
}
