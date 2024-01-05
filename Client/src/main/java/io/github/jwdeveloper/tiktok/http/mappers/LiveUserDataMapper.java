package io.github.jwdeveloper.tiktok.http.mappers;

import com.google.gson.JsonParser;
import io.github.jwdeveloper.tiktok.data.requests.LiveUserData;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;

public class LiveUserDataMapper {


    public LiveUserData.Response map(String json) {
        var parsedJson = JsonParser.parseString(json);
        var jsonObject = parsedJson.getAsJsonObject();

        var message = jsonObject.get("message").getAsString();

        if (message.equals("params_error")) {
            throw new TikTokLiveRequestException("fetchRoomIdFromTiktokApi -> Unable to fetch roomID, contact the developer");
        }
        if (message.equals("user_not_found")) {
            return new LiveUserData.Response(json, LiveUserData.UserStatus.NotFound, "", -1);
        }
        //live -> status 2
        //live paused -> 3
        //not live -> status 4
        var element = jsonObject.get("data");
        if (element.isJsonNull()) {
            return new LiveUserData.Response(json, LiveUserData.UserStatus.NotFound, "", -1);
        }
        var data = element.getAsJsonObject();
        var user = data.getAsJsonObject("user");
        var roomId = user.get("roomId").getAsString();
        var status = user.get("status").getAsInt();

        var liveRoom = data.getAsJsonObject("liveRoom");
        long startTime = liveRoom.get("startTime").getAsLong();

        var statusEnum = switch (status) {
            case 2 -> LiveUserData.UserStatus.Live;
            case 3 -> LiveUserData.UserStatus.LivePaused;
            case 4 -> LiveUserData.UserStatus.Offline;
            default -> LiveUserData.UserStatus.NotFound;
        };

        return new LiveUserData.Response(json, statusEnum, roomId, startTime);

    }
}
