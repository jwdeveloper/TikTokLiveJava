package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.messages.WebcastGiftBroadcastMessage;
import lombok.Getter;

@Getter
public class TikTokGiftBroadcastEvent extends TikTokEvent
{
  private final Picture picture;

  private final String shortURL;

  private final String notifyEventType;

  private final String notifyLabel;

  private final String notifyType;

  public TikTokGiftBroadcastEvent(WebcastGiftBroadcastMessage msg)
  {
    super(msg.getHeader());
    picture = new Picture(msg.getPicture());
    var data = msg.getData();
    shortURL = data.getUri();
    notifyEventType = data.getRoomNotifyMessage().getData().getType();
    notifyLabel = data.getRoomNotifyMessage().getData().getLabel();
    notifyType = data.getNotifyType();
  }
}
