package io.github.jwdeveloper.tiktok.handlers;


import com.google.protobuf.ByteString;
import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.TikTokLiveClient;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokUnhandledEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokMessageMappingException;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;

import java.util.Arrays;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.logging.Logger;


public abstract class TikTokMessageHandler {

    private final Map<String, io.github.jwdeveloper.tiktok.handler.TikTokMessageHandler> handlers;
    private final TikTokEventHandler tikTokEventHandler;
    private final ClientSettings clientSettings;
    protected final Logger logger;

    public TikTokMessageHandler(TikTokEventHandler tikTokEventHandler,ClientSettings clientSettings, Logger logger) {
        handlers = new HashMap<>();
        this.tikTokEventHandler = tikTokEventHandler;
        this.clientSettings = clientSettings;
        this.logger = logger;
        init();
    }

    public abstract void init();

    public void register(Class<?> clazz, Function<WebcastResponse.Message, TikTokEvent> func) {
        handlers.put(clazz.getSimpleName(), func::apply);
    }

    public void register(Class<?> input, Class<?> output) {
        register(input, (e) -> mapMessageToEvent(input, output, e));
    }

    public void handle(TikTokLiveClient client, WebcastResponse webcastResponse) {
        for (var message : webcastResponse.getMessagesList()) {
            try
            {
                if(clientSettings.isPrintMessageData())
                {
                    var type=  message.getType();
                    var base64 =     Base64.getEncoder().encodeToString(message.getBinary().toByteArray());
                    logger.info(type+": \n "+base64);
                }
                handleSingleMessage(client, message);
            } catch (Exception e) {
                var exception = new TikTokLiveMessageException(message, webcastResponse, e);
                tikTokEventHandler.publish(client, new TikTokErrorEvent(exception));
            }
        }
    }

    private void handleSingleMessage(TikTokLiveClient client, WebcastResponse.Message message) throws Exception {
        if (!handlers.containsKey(message.getType())) {
            tikTokEventHandler.publish(client, new TikTokUnhandledEvent(message));
            return;
        }
        var handler = handlers.get(message.getType());
        var tiktokEvent = handler.handle(message);
        tikTokEventHandler.publish(client, tiktokEvent);
    }

    protected TikTokEvent mapMessageToEvent(Class<?> inputClazz, Class<?> outputClass, WebcastResponse.Message message) {
        try {
            var parseMethod = inputClazz.getDeclaredMethod("parseFrom", ByteString.class);
            var deserialized = parseMethod.invoke(null, message.getBinary());

            var constructors = Arrays.stream(outputClass.getConstructors())
                    .filter(ea -> Arrays.stream(ea.getParameterTypes())
                            .toList()
                            .contains(inputClazz))
                    .findFirst();

            if(constructors.isEmpty())
            {
                throw new TikTokMessageMappingException(inputClazz, outputClass, "Unable to find constructor with input class type");
            }

            var tiktokEvent = constructors.get().newInstance(deserialized);
            return (TikTokEvent) tiktokEvent;
        } catch (Exception ex) {
            throw new TikTokMessageMappingException(inputClazz, outputClass, ex);
        }
    }
}
