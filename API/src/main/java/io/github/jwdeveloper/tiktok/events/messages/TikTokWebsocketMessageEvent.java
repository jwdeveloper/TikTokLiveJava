package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;


/**
 * Happens when TikTok websocket receive message from server
 */
@Getter
@AllArgsConstructor
@EventMeta(eventType = EventType.Custom)
public class TikTokWebsocketMessageEvent extends TikTokEvent
{
    private TikTokEvent event;

    private WebcastResponse.Message message;
}
