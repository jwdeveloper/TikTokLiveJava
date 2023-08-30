package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TikTokSuccessResponseMappingEvent extends TikTokEvent
{
    private TikTokEvent event;

    private WebcastResponse.Message message;
}
