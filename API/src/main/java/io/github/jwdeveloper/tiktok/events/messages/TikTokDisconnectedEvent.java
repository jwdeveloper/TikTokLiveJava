package io.github.jwdeveloper.tiktok.events.messages;
import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokLiveClientEvent;

@EventMeta(eventType = EventType.Control)
public class TikTokDisconnectedEvent extends TikTokLiveClientEvent {
}
