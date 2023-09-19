package io.github.jwdeveloper.tiktok.mappers;

import com.google.gson.JsonObject;
import io.github.jwdeveloper.tiktok.live.LiveRoomMeta;

public class LiveRoomMetaMapper implements Mapper<JsonObject, LiveRoomMeta> 
{
    @Override
    public LiveRoomMeta mapFrom(JsonObject input) {
        var liveRoomMeta = new LiveRoomMeta();

        if (!input.has("data")) {
            return liveRoomMeta;
        }
        var data = input.getAsJsonObject("data");
        if (data.has("status")) {
            var status = data.get("status");
            liveRoomMeta.setStatus(status.getAsInt());
        }


        if(data.has("age_restricted"))
        {
            var element = data.getAsJsonObject("age_restricted");
            var restricted= element.get("restricted").getAsBoolean();
            liveRoomMeta.setAgeRestricted(restricted);
        }
        return liveRoomMeta;
    }
}
