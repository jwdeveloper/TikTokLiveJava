package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastSocialMessage;
import lombok.Value;

@Value
@EventMeta(eventType = EventType.Custom)
public class TikTokFollowEvent extends TikTokHeaderEvent
{
   User newFollower;
   Long totalFollowers;

  public TikTokFollowEvent(WebcastSocialMessage msg) {
    super(msg.getHeader());
    newFollower = User.MapOrEmpty(msg.getSender());
    totalFollowers = msg.getTotalFollowers();
  }
}
