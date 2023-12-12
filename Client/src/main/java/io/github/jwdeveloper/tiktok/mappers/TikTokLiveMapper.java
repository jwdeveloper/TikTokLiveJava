/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
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
