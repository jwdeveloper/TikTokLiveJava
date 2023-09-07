package io.github.jwdeveloper.tiktok.events.base;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TikTokUnhandledEvent<T> extends TikTokEvent
{
    private final T data;
}
