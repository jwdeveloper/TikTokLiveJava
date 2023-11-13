package io.github.jwdeveloper.tiktok.data.events.room;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;


/*
Triggered when LiveRoomInfo got updated such as likes, viewers, ranking ....
 */
@Getter
@AllArgsConstructor
@EventMeta(eventType = EventType.Message)
public class TikTokRoomInfoEvent extends TikTokEvent
{
    LiveRoomInfo roomInfo;
}
