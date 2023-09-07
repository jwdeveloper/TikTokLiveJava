package io.github.jwdeveloper.tiktok.events.messages;
import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;

@EventMeta(eventType = EventType.Custom)
public class TikTokLiveEndedEvent extends TikTokEvent {
}
