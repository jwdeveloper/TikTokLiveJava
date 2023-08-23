package io.github.jwdeveloper.tiktok.handler;


import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;

public interface TikTokMessageHandler
{
    TikTokEvent handle(WebcastResponse.Message message) throws Exception;
}
