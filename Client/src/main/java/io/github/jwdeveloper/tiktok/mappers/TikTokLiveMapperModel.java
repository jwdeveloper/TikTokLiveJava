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

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.mappers.data.MappingAction;
import io.github.jwdeveloper.tiktok.mappers.data.MappingResult;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.function.Function;

@Getter
@Setter
public class TikTokLiveMapperModel implements TikTokMapperModel {
    @Getter
    private String sourceMessageName;

    private MappingAction<byte[]> onBeforeMapping;

    private MappingAction<MappingResult> onMapping;

    private Function<MappingResult, List<TikTokEvent>> onAfterMapping;

    public TikTokLiveMapperModel(String sourceMessageName, MappingAction onMapping) {
        this.sourceMessageName = sourceMessageName;
        this.onBeforeMapping = (inputBytes, mesasgeName, mapperHelper) -> inputBytes;
        this.onMapping = onMapping;
        this.onAfterMapping = MappingResult::getEvents;
    }

    public TikTokLiveMapperModel(String sourceMessageName) {
        this.sourceMessageName = sourceMessageName;
        this.onBeforeMapping = (inputBytes, mesasgeName, mapperHelper) -> inputBytes;
        this.onMapping = (inputBytes, mesasgeName, mapperHelper) -> MappingResult.of(inputBytes, List.of());
        this.onAfterMapping = MappingResult::getEvents;
    }

    @Override
    public TikTokMapperModel onBeforeMapping(MappingAction<byte[]> action) {
        this.onBeforeMapping = action;
        return this;
    }

    @Override
    public TikTokMapperModel onMapping(MappingAction<MappingResult> action) {
        this.onMapping = action;
        return this;
    }

    @Override
    public TikTokMapperModel onAfterMapping(Function<MappingResult, List<TikTokEvent>> action) {
        this.onAfterMapping = action;
        return this;
    }

}
