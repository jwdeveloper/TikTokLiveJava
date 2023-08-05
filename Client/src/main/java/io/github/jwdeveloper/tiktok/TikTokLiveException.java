package io.github.jwdeveloper.tiktok;

public class TikTokLiveException extends RuntimeException
{
    public TikTokLiveException() {
    }

    public TikTokLiveException(String message) {
        super(message);
    }

    public TikTokLiveException(String message, Throwable cause) {
        super(message, cause);
    }

    public TikTokLiveException(Throwable cause) {
        super(cause);
    }

    public TikTokLiveException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
