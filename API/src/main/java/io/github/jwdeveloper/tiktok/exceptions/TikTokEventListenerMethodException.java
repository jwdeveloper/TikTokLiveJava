package io.github.jwdeveloper.tiktok.exceptions;

public class TikTokEventListenerMethodException extends TikTokLiveException
{
    public TikTokEventListenerMethodException() {
    }

    public TikTokEventListenerMethodException(String message) {
        super(message);
    }

    public TikTokEventListenerMethodException(String message, Throwable cause) {
        super(message, cause);
    }

    public TikTokEventListenerMethodException(Throwable cause) {
        super(cause);
    }

    public TikTokEventListenerMethodException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
