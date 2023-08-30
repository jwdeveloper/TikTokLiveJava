package io.github.jwdeveloper.tiktok.exceptions;

import io.github.jwdeveloper.tiktok.messages.WebcastResponse;
import lombok.Getter;

import java.util.Base64;

public class TikTokLiveMessageException extends TikTokLiveException {

    @Getter
    private final WebcastResponse.Message webcastMessage;
    @Getter
    private final WebcastResponse webcastResponse;


    public TikTokLiveMessageException(WebcastResponse.Message message,
                                      WebcastResponse webcastResponse,
                                      Throwable cause) {
        super("Error while handling Message: " + message.getType() + ": \n", cause);
        this.webcastMessage = message;
        this.webcastResponse = webcastResponse;
    }

    public String messageName()
    {
        return webcastMessage.getType();
    }

    public String messageToBase64()
    {
        return Base64.getEncoder().encodeToString(webcastMessage.getBinary().toByteArray());
    }

    public String webcastResponseToBase64()
    {
        return  Base64.getEncoder().encodeToString(webcastResponse.toByteArray());
    }
}
