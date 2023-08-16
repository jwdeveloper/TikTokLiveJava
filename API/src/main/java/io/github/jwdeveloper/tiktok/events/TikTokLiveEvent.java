package io.github.jwdeveloper.tiktok.events;

import io.github.jwdeveloper.tiktok.live.LiveClient;

public interface TikTokLiveEvent<T extends TikTokEvent>
{
     void onEvent(LiveClient liveClient, T event);
}
