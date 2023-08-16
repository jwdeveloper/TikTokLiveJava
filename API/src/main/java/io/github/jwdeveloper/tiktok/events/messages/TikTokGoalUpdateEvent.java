package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.Picture;

import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastGoalUpdateMessage;
import lombok.Getter;

import java.util.List;

@Getter
public class TikTokGoalUpdateEvent extends TikTokEvent
{
  private final Long goalId;
  private final Picture picture;
  private final String eventType;
  private final String label;
  private final List<User> users;

  public TikTokGoalUpdateEvent(WebcastGoalUpdateMessage msg) {
    super(msg.getHeader());
    picture = new Picture(msg.getPicture());
    goalId = msg.getId();
    eventType = msg.getData().getType();
    label = msg.getUpdateData().getLabel();
    users = msg.getUpdateData().getUsersList().stream().map(u ->new User(u.getId(),u.getNickname(),new Picture(u.getProfilePicture()))).toList();
  }
}
