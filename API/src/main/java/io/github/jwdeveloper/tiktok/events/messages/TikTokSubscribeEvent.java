package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastMemberMessage;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Custom)
public class TikTokSubscribeEvent extends TikTokHeaderEvent {
  private User newSubscriber;

  public TikTokSubscribeEvent(WebcastMemberMessage msg) {
    super(msg.getHeader());

    if(msg.hasUser())
    {
      newSubscriber = new User(msg.getUser());
    }
  }

}
