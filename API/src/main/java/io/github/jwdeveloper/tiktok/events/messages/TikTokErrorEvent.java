package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TikTokErrorEvent extends TikTokEvent
{
    private final Throwable exception;
}
