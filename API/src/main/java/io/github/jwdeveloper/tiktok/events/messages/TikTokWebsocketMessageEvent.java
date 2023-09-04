package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;
import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * Happens when TikTok websocket receive message from server
 */
@Data
@AllArgsConstructor
public class TikTokWebsocketMessageEvent extends TikTokEvent
{
    private TikTokEvent event;

    private WebcastResponse.Message message;
}
