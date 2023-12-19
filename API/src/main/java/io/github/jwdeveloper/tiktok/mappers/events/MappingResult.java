package io.github.jwdeveloper.tiktok.mappers.events;

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@AllArgsConstructor
@Getter
public class MappingResult
{

    Object source;

    List<TikTokEvent> events;

    String message;

    public static MappingResult of(Object source) {
        return new MappingResult(source, List.of(),"");
    }

    public static MappingResult of(Object source, List<TikTokEvent> events) {
        return new MappingResult(source, events,"");
    }

    public static MappingResult of(Object source,TikTokEvent events) {
        return new MappingResult(source, List.of(events),"");
    }
}
