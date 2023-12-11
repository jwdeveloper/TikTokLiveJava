package io.github.jwdeveloper.tiktok.mappers;

import com.google.protobuf.GeneratedMessageV3;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokMessageMappingException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TikTokLiveMapper implements TikTokMapper {
    private final Map<String, Function<byte[], List<TikTokEvent>>> mappers;
    private final TikTokGenericEventMapper genericMapper;

    public TikTokLiveMapper(TikTokGenericEventMapper genericMapper) {
        this.mappers = new HashMap<>();
        this.genericMapper = genericMapper;
    }

    @Override
    public void bytesToEvent(String messageName, Function<byte[], TikTokEvent> onMapping) {
        mappers.put(messageName, messagePayload -> List.of(onMapping.apply(messagePayload)));
    }

    @Override
    public void bytesToEvents(String messageName, Function<byte[], List<TikTokEvent>> onMapping) {
        mappers.put(messageName, onMapping::apply);
    }

    public void bytesToEvent(Class<? extends GeneratedMessageV3> clazz, Function<byte[], TikTokEvent> onMapping) {
        mappers.put(clazz.getSimpleName(), messagePayload -> List.of(onMapping.apply(messagePayload)));
    }



    public void bytesToEvents(Class<? extends GeneratedMessageV3> clazz, Function<byte[], List<TikTokEvent>> onMapping) {
        mappers.put(clazz.getSimpleName(), onMapping::apply);
    }

    @Override
    public void webcastObjectToConstructor(Class<? extends GeneratedMessageV3> sourceClass, Class<? extends TikTokEvent> outputClass) {
        bytesToEvent(sourceClass, (e) -> genericMapper.mapToEvent(sourceClass, outputClass, e));
    }

    @Override
    public <T extends GeneratedMessageV3> void webcastObjectToEvent(Class<T> source, Function<T, TikTokEvent> onMapping) {
        bytesToEvent(source, (bytes) ->
        {
            try {
                var parsingMethod = genericMapper.getParsingMethod(source);
                var sourceObject = parsingMethod.invoke(null, bytes);
                var event = onMapping.apply((T) sourceObject);
                return event;
            } catch (Exception e) {
                throw new TikTokMessageMappingException(source, "can't find parsing method", e);
            }
        });
    }

    @Override
    public <T extends GeneratedMessageV3> void webcastObjectToEvents(Class<T> source, Function<T, List<TikTokEvent>> onMapping) {
        bytesToEvents(source, (bytes) ->
        {
            try {
                var parsingMethod = genericMapper.getParsingMethod(source);
                var sourceObject = parsingMethod.invoke(null, bytes);
                var event = onMapping.apply((T) sourceObject);
                return event;
            } catch (Exception e) {
                throw new TikTokMessageMappingException(source, "can't find parsing method", e);
            }
        });
    }

    public boolean isRegistered(String input) {
        return mappers.containsKey(input);
    }

    public List<TikTokEvent> handleMapping(String input, byte[] bytes) {
        if (!isRegistered(input)) {
            return List.of();
        }
        var mapper = mappers.get(input);
        var events = mapper.apply(bytes);
        return events;
    }
}
