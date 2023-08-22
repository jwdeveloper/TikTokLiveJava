package io.github.jwdeveloper.tiktok.exceptions;

public class TikTokLiveRequestException extends TikTokLiveException
{
    public TikTokLiveRequestException() {
    }

    public TikTokLiveRequestException(String message) {
        super(message);
    }

    public TikTokLiveRequestException(String message, Throwable cause) {
        super(message, cause);
    }

    public TikTokLiveRequestException(Throwable cause) {
        super(cause);
    }

    public TikTokLiveRequestException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
