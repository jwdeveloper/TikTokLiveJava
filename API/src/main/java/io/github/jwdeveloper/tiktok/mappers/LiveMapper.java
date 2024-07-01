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
import io.github.jwdeveloper.tiktok.mappers.data.MappingAction;
import io.github.jwdeveloper.tiktok.mappers.data.MappingResult;

import java.util.List;
import java.util.function.Function;

public interface LiveMapper {

    /**
     * when mapper is not found for messageName, TikTokLiveException is thrown
     *
     * @param messageName
     * @return TikTokMapperModel
     */
    TikTokMapperModel forMessage(String messageName);

    /**
     * @param mapperName protocol buffer class type
     * @return
     */
    TikTokMapperModel forMessage(Class<? extends GeneratedMessageV3> mapperName);

    TikTokMapperModel forMessage(String mapperName, MappingAction<MappingResult> onMapping);

    TikTokMapperModel forMessage(Class<? extends GeneratedMessageV3> mapperName, MappingAction<MappingResult> onMapping);

    TikTokMapperModel forMessage(Class<? extends GeneratedMessageV3> mapperName, Function<byte[], TikTokEvent> onMapping);

    TikTokMapperModel forAnyMessage();

    List<TikTokEvent> handleMapping(String messageName, byte[] bytes);

    boolean isRegistered(String mapperName);

    <T extends GeneratedMessageV3> boolean isRegistered(Class<T> mapperName);
}
