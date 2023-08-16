package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastMemberMessage;
import lombok.Getter;

@Getter
public class TikTokSubscribeEvent extends TikTokEvent {
  private User newSubscriber;

  public TikTokSubscribeEvent(WebcastMemberMessage msg) {
    super(msg.getHeader());

    if(msg.hasUser())
    {
      newSubscriber = new User(msg.getUser());
    }
  }

}
