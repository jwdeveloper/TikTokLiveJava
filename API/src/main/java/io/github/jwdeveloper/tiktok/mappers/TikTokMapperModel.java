package io.github.jwdeveloper.tiktok.mappers;

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.mappers.events.AfterMappingAction;
import io.github.jwdeveloper.tiktok.mappers.events.MappingAction;
import io.github.jwdeveloper.tiktok.mappers.events.MappingResult;

import java.util.List;
import java.util.function.Function;

public interface TikTokMapperModel {

    /**
     * @return name of websocket message that this mapper is mapping from
     */

    String getSourceMessageName();

    /**
     * @param action Input bytes from websocket, you can modify it and returns different bytes
     */
    TikTokMapperModel onBeforeMapping(MappingAction<byte[]> action);

    /**
     * @param action Input bytes from websocket. As output returns list of tiktok live events
     */
    TikTokMapperModel onMapping(MappingAction<MappingResult> action);



    /**
     * @param action You can modify output list of TikTokLive events
     * @see AfterMappingAction
     */
    TikTokMapperModel onAfterMapping(Function<MappingResult, List<TikTokEvent>> action);
}
