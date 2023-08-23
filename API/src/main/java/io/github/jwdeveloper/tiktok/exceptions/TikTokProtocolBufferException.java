package io.github.jwdeveloper.tiktok.exceptions;

import lombok.Getter;

public class TikTokProtocolBufferException extends TikTokLiveException
{
    @Getter
    private final byte[] bytes;

    public TikTokProtocolBufferException(String message, byte[] bytes, Throwable cause)
    {
        super(message, cause);
        this.bytes = bytes;
    }
}
