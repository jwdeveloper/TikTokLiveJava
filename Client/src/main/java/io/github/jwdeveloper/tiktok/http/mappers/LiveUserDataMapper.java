/*
 * Copyright (c) 2023-2024 jwdeveloper jacekwoln@gmail.com
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
package io.github.jwdeveloper.tiktok.http.mappers;

import com.google.gson.*;
import io.github.jwdeveloper.tiktok.*;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.requests.LiveUserData;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;

import java.util.List;
import java.util.logging.Logger;

public class LiveUserDataMapper
{
    public static LiveUserData.Response map(String json, Logger logger) {
        try {
            var jsonObject = JsonParser.parseString(json).getAsJsonObject();

            var message = jsonObject.get("message").getAsString();

            if (message.equals("params_error")) {
                throw new TikTokLiveRequestException("fetchRoomIdFromTiktokApi -> Unable to fetch roomID, contact the developer");
            }
            if (message.equals("user_not_found")) {
                return new LiveUserData.Response(json, LiveUserData.UserStatus.NotFound, null);
            }
            //live -> status 2
            //live paused -> 3
            //not live -> status 4
            var element = jsonObject.get("data");
            if (element.isJsonNull()) {
                return new LiveUserData.Response(json, LiveUserData.UserStatus.NotFound, null);
            }
            var data = element.getAsJsonObject();
            var user = data.getAsJsonObject("user");
            var stats = data.getAsJsonObject("stats");
            var roomId = user.get("roomId").getAsString();
            var status = user.get("status").getAsInt();

            TikTokRoomInfo roomInfo = new TikTokRoomInfo();
            roomInfo.setRoomId(roomId);

            var liveRoom = data.getAsJsonObject("liveRoom");

            roomInfo.setTitle(liveRoom.get("title").getAsString());
            roomInfo.setStartTime(liveRoom.get("startTime").getAsLong());
            roomInfo.setViewersCount(liveRoom.getAsJsonObject("liveRoomStats").get("userCount").getAsInt());
            roomInfo.setTotalViewersCount(liveRoom.getAsJsonObject("liveRoomStats").get("enterCount").getAsInt());
            roomInfo.setAgeRestricted(jsonObject.get("statusCode").getAsInt() == TikTokLiveHttpClient.TIKTOK_AGE_RESTRICTED_CODE);

            var statusEnum = switch (status) {
                case 2 -> LiveUserData.UserStatus.Live;
                case 3 -> LiveUserData.UserStatus.LivePaused;
                case 4 -> LiveUserData.UserStatus.Offline;
                default -> LiveUserData.UserStatus.NotFound;
            };

            User foundUser = new User(
                Long.parseLong(user.get("id").getAsString()),
                user.get("uniqueId").getAsString(),
                user.get("nickname").getAsString(),
                user.get("signature").getAsString(),
                new Picture(user.get("avatarLarger").getAsString()),
                stats.get("followingCount").getAsLong(),
                stats.get("followerCount").getAsLong(),
                List.of());

            roomInfo.setHost(foundUser);
            roomInfo.setHostName(foundUser.getName());

            return new LiveUserData.Response(json, statusEnum, roomInfo);
        } catch (JsonSyntaxException | IllegalStateException | NullPointerException e) {
            logger.warning("Malformed Json: '"+json+"' - Error Message: "+e.getMessage());
            return new LiveUserData.Response(json, LiveUserData.UserStatus.NotFound, null);
        }
    }

    public static LiveUserData.Response mapEulerstream(JsonObject jsonObject, Logger logger) {
        try {
			JsonObject roomInfoJson = jsonObject.getAsJsonObject("roomInfo");
			JsonObject userJson = jsonObject.getAsJsonObject("user");

            var roomId = roomInfoJson.get("id").getAsString();
            var status = roomInfoJson.get("status").getAsInt();

            TikTokRoomInfo roomInfo = new TikTokRoomInfo();
            roomInfo.setRoomId(roomId);
            roomInfo.setTitle(roomInfoJson.get("title").getAsString());
            roomInfo.setStartTime(roomInfoJson.get("startTime").getAsLong());
            roomInfo.setViewersCount(roomInfoJson.get("currentViewers").getAsInt());
            roomInfo.setTotalViewersCount(roomInfoJson.get("totalViewers").getAsInt());

            var statusEnum = switch (status) {
                case 2 -> LiveUserData.UserStatus.Live;
                case 3 -> LiveUserData.UserStatus.LivePaused;
                case 4 -> LiveUserData.UserStatus.Offline;
                default -> LiveUserData.UserStatus.NotFound;
            };

            User foundUser = new User(
                Long.parseLong(userJson.get("numericUid").getAsString()),
                userJson.get("uniqueId").getAsString(),
                userJson.get("nickname").getAsString(),
                userJson.get("signature").getAsString(),
                new Picture(userJson.get("avatarUrl").getAsString()),
                userJson.get("following").getAsLong(),
                userJson.get("followers").getAsLong(),
                List.of());

            roomInfo.setHost(foundUser);
            roomInfo.setHostName(foundUser.getName());

            return new LiveUserData.Response(jsonObject.toString(), statusEnum, roomInfo);
        } catch (JsonSyntaxException | IllegalStateException | NullPointerException e) {
            logger.warning("Malformed Json: '"+jsonObject.toString()+"' - Error Message: "+e.getMessage());
            return new LiveUserData.Response(jsonObject.toString(), LiveUserData.UserStatus.NotFound, null);
        }
    }
}