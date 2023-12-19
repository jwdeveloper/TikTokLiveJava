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
import io.github.jwdeveloper.tiktok.mappers.events.MappingAction;
import io.github.jwdeveloper.tiktok.mappers.events.MappingResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class TikTokLiveMapper implements TikTokMapper {

    private final Map<String, TikTokLiveMapperModel> mappers;
    private final TikTokMapperHelper mapperUtils;

    public TikTokLiveMapper(TikTokMapperHelper mapperUtils) {
        this.mappers = new HashMap<>();
        this.mapperUtils = mapperUtils;
    }

    @Override
    public TikTokMapperModel forMessage(String messageName) {
        if (!isRegistered(messageName)) {
            var model = new TikTokLiveMapperModel(messageName);
            mappers.put(messageName, model);
        }
        return mappers.get(messageName);
    }

    @Override
    public TikTokMapperModel forMessage(Class<? extends GeneratedMessageV3> mapperName) {
        return forMessage(mapperName.getSimpleName());
    }

    @Override
    public TikTokMapperModel forMessage(String mapperName, MappingAction<MappingResult> onMapping) {
        var model = forMessage(mapperName);
        model.onMapping(onMapping);
        return model;
    }


    @Override
    public TikTokMapperModel forMessage(Class<? extends GeneratedMessageV3> mapperName, MappingAction<MappingResult> onMapping) {
        var model = forMessage(mapperName);
        model.onMapping(onMapping);
        return model;
    }

    @Override
    public TikTokMapperModel forMessage(Class<? extends GeneratedMessageV3> mapperName, Function<byte[], TikTokEvent> onMapping) {
        return forMessage(mapperName, (inputBytes, messageName, mapperHelper) -> MappingResult.of(inputBytes, onMapping.apply(inputBytes)));
    }


    public boolean isRegistered(String mapperName) {
        return mappers.containsKey(mapperName);
    }

    public <T extends GeneratedMessageV3> boolean isRegistered(Class<T> mapperName) {
        return mappers.containsKey(mapperName.getSimpleName());
    }
    public List<TikTokEvent> handleMapping(String messageName, byte[] bytes) {
        if (!isRegistered(messageName)) {
            return List.of();
        }
        var mapperModel = mappers.get(messageName);

        var inputBytes = mapperModel.getOnBeforeMapping().onMapping(bytes, messageName, mapperUtils);

        var mappingResult = mapperModel.getOnMapping().onMapping(inputBytes, messageName, mapperUtils);

        var afterMappingResult = mapperModel.getOnAfterMapping().apply(mappingResult);
        return afterMappingResult;
    }
}
