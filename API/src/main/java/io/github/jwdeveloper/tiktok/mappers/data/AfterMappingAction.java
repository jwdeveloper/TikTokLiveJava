package io.github.jwdeveloper.tiktok.mappers.data;

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;

import java.util.List;

@FunctionalInterface
public interface AfterMappingAction {
    /**
     * @param source object that was used as source to create events
     * @param events list of events prepared before, could be modified or changed
     * @return list of events that will be invoked
     */
    List<TikTokEvent> onAfterMapping(Object source, List<TikTokEvent> events);
}
