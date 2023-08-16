package io.github.jwdeveloper.tiktok.handlers;


import com.google.protobuf.ByteString;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokUnhandledEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageParsingException;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


public abstract class WebResponseHandlerBase {

    private final Map<String, TikTokMessageHandler> handlers;
    private final TikTokEventHandler tikTokEventHandler;

    public WebResponseHandlerBase(TikTokEventHandler tikTokEventHandler) {
        handlers = new HashMap<>();
        this.tikTokEventHandler = tikTokEventHandler;
        init();
    }

    public abstract void init();

    public void register(Class<?> input, Class<?> output) {
        register(input, (e) ->
        {
            try {
                var parseMethod = input.getDeclaredMethod("parseFrom", ByteString.class);
                var deserialized = parseMethod.invoke(null, e.getBinary());

                var constructors = Arrays.stream(output.getConstructors()).filter(ea -> Arrays.stream(ea.getParameterTypes()).toList().contains(input)).findFirst();

                var tiktokEvent = constructors.get().newInstance(deserialized);
                return (TikTokEvent)tiktokEvent;
            } catch (Exception ex)
            {
                throw new TikTokLiveMessageParsingException("Unable to handle parsing from class: " + input.getSimpleName() + " to class " + output.getSimpleName(), ex);
            }
        });
    }

    public <T> void register(Class clazz, Function<WebcastResponse.Message, TikTokEvent> func) {
        var haandler = new TikTokMessageHandler<T>() {
            @Override
            public Class<T> getHandleClazz() {
                return clazz;
            }

            @Override
            public TikTokEvent handle(WebcastResponse.Message message) throws Exception {
                return func.apply(message);
            }
        };

        handlers.put(haandler.getHandleClazz().getSimpleName(), haandler);
    }

    public void handle(WebcastResponse webcastResponse) {
        for (var message : webcastResponse.getMessagesList()) {
            try {
                handleSingleMessage(message);
            } catch (Exception e)
            {
                var decoded = Base64.getEncoder().encodeToString(message.getBinary().toByteArray());

                var exception = new TikTokLiveException("Error whilst Handling Message. Stopping Client. Final Message: \n"+decoded, e);
                tikTokEventHandler.publish(new TikTokErrorEvent(exception));
            }
        }
    }

    private void handleSingleMessage(WebcastResponse.Message message) throws Exception {
        if (!handlers.containsKey(message.getType())) {
            tikTokEventHandler.publish(new TikTokUnhandledEvent(message));
            return;
        }
        var handler = handlers.get(message.getType());
        var tiktokEvent = handler.handle(message);
        tikTokEventHandler.publish(tiktokEvent);
    }
}
