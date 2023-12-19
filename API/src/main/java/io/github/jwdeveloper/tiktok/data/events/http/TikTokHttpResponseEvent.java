package io.github.jwdeveloper.tiktok.data.events.http;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.models.http.HttpData;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@EventMeta(eventType = EventType.Debug)
public class TikTokHttpResponseEvent extends TikTokEvent
{
    String url;

    HttpData response;

    HttpData request;
}
