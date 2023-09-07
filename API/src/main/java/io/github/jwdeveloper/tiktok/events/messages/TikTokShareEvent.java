package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastSocialMessage;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Custom)
public class TikTokShareEvent extends TikTokHeaderEvent {
  private final User user;
  private final Integer amount;

  public TikTokShareEvent(WebcastSocialMessage msg, Integer amount) {
    super(msg.getHeader());;
    user = User.MapOrEmpty(msg.getSender());
    this.amount = amount;
  }

  public TikTokShareEvent(WebcastSocialMessage msg) {
    super(msg.getHeader());
    user = User.MapOrEmpty(msg.getSender());
    amount = 1;
  }

}
