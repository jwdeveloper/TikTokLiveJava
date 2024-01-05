package io.github.jwdeveloper.tiktok.exceptions;

public class TikTokSignServerException extends TikTokLiveRequestException
{
    public TikTokSignServerException() {
    }

    public TikTokSignServerException(String message) {
        super(message);
    }

    public TikTokSignServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public TikTokSignServerException(Throwable cause) {
        super(cause);
    }

    public TikTokSignServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
