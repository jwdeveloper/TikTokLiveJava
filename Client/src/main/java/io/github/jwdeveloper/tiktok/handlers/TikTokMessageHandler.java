package io.github.jwdeveloper.tiktok.handlers;


import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;

public interface TikTokMessageHandler<T>
{
     Class<T> getHandleClazz();

    TikTokEvent handle(WebcastResponse.Message message) throws Exception;
}
