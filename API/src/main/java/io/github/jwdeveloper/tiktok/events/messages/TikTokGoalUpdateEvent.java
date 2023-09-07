package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.Picture;

import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastGoalUpdateMessage;
import lombok.Getter;

import java.util.List;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokGoalUpdateEvent extends TikTokHeaderEvent
{
  private final Long goalId;
  private final Picture picture;
  private final String eventType;
  private final String label;
  private final List<User> users;

  public TikTokGoalUpdateEvent(WebcastGoalUpdateMessage msg) {
    super(msg.getHeader());
    picture = new Picture(msg.getImage());
    goalId = msg.getId();
    eventType = msg.getData().getType();
    label = msg.getUpdateData().getLabel();
    users = msg.getUpdateData().getUsersList().stream().map(u ->new User(u.getId(),u.getNickname(),new Picture(u.getProfileImage()))).toList();
  }
}
