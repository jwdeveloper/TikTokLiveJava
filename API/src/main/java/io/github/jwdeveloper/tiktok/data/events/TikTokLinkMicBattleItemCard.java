package io.github.jwdeveloper.tiktok.data.events;

import io.github.jwdeveloper.tiktok.annotations.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMicBattleItemCard;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokLinkMicBattleItemCard extends TikTokHeaderEvent {

	public TikTokLinkMicBattleItemCard(WebcastLinkMicBattleItemCard msg) {
		super(msg.getCommon());
	}
}