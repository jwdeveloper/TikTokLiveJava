package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.messages.WebcastGiftBroadcastMessage;
import lombok.Getter;
import lombok.Value;

@Value
@EventMeta(eventType = EventType.Message)
public class TikTokGiftBroadcastEvent extends TikTokHeaderEvent
{
    Picture picture;

    String shortURL;

    String notifyEventType;

    String notifyLabel;

    String notifyType;

  public TikTokGiftBroadcastEvent(WebcastGiftBroadcastMessage msg)
  {
    super(msg.getHeader());
    picture = new Picture(msg.getImage());
    var data = msg.getData();
    shortURL = data.getUri();
    notifyEventType = data.getRoomNotifyMessage().getData().getType();
    notifyLabel = data.getRoomNotifyMessage().getData().getLabel();
    notifyType = data.getNotifyType();
  }
}
