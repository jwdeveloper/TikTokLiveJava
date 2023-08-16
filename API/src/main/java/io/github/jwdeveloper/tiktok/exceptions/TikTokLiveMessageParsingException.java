package io.github.jwdeveloper.tiktok.exceptions;

public class TikTokLiveMessageParsingException extends TikTokLiveException
{
    public TikTokLiveMessageParsingException() {
    }

    public TikTokLiveMessageParsingException(String message) {
        super(message);
    }

    public TikTokLiveMessageParsingException(String message, Throwable cause) {
        super(message, cause);
    }

    public TikTokLiveMessageParsingException(Throwable cause) {
        super(cause);
    }

    public TikTokLiveMessageParsingException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
