package io.github.jwdeveloper.tiktok.data.events;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;


@EventMeta(eventType = EventType.Message)
public class TikTokLiveUnpausedEvent extends TikTokEvent {
}
