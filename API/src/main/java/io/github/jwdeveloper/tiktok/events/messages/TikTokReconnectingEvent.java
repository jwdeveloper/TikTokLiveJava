package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokLiveClientEvent;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Control)
public class TikTokReconnectingEvent extends TikTokLiveClientEvent
{

}
