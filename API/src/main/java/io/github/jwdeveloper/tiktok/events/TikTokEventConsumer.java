package io.github.jwdeveloper.tiktok.events;

import io.github.jwdeveloper.tiktok.live.LiveClient;

public interface TikTokEventConsumer<T extends TikTokEvent>
{
     void onEvent(LiveClient liveClient, T event);
}
