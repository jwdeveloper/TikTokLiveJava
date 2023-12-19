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
