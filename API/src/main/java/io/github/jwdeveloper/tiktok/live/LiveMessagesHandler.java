package io.github.jwdeveloper.tiktok.live;

import io.github.jwdeveloper.tiktok.data.dto.MessageMetaData;
import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketMessageEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.websocket.TikTokWebsocketUnhandledMessageEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageException;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.utils.Stopwatch;

import java.time.Duration;

public interface LiveMessagesHandler {
    void handle(LiveClient client, WebcastResponse webcastResponse);

    void handleSingleMessage(LiveClient client, WebcastResponse.Message message);
}
