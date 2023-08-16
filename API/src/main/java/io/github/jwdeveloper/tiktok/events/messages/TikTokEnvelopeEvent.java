package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastEnvelopeMessage;
import lombok.Getter;

@Getter
public class TikTokEnvelopeEvent extends TikTokEvent {
  private final User user;

  public TikTokEnvelopeEvent(WebcastEnvelopeMessage msg) {
    super(msg.getHeader());
    user = new User(msg.getUser().getId(), msg.getUser().getUsername());
  }
}
