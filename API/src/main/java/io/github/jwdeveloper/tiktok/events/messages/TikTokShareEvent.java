package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastSocialMessage;
import lombok.Getter;

@Getter
public class TikTokShareEvent extends TikTokEvent {
  private User user;

  private Integer amount;

  public TikTokShareEvent(WebcastSocialMessage msg, Integer amount) {
    super(msg.getHeader());;
    if(msg.hasSender())
    {
      user = new User(msg.getSender());
    }
    this.amount = amount;
  }

  public TikTokShareEvent(WebcastSocialMessage msg) {
    super(msg.getHeader());
    if(msg.hasSender())
    {
      user = new User(msg.getSender());
    }
    amount = 1;
  }

}
