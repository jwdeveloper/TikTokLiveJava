package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.Nullable;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastSocialMessage;
import lombok.Getter;

@Getter
public class TikTokFollowEvent extends TikTokEvent
{
  @Nullable
  private User newFollower;
  private final Long totalFollowers;

  public TikTokFollowEvent(WebcastSocialMessage msg) {
    super(msg.getHeader());
    if(msg.hasSender())
    {
      newFollower = new User(msg.getSender());
    }
    totalFollowers = msg.getTotalFollowers();

  }
}
