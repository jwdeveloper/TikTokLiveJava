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
package io.github.jwdeveloper.tiktok.http.mappers;

import com.google.gson.*;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.requests.LiveUserData;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;

import java.util.List;
import java.util.logging.Logger;

public class LiveUserDataMapper
{
    public LiveUserData.Response map(String json, Logger logger) {
        try {
            var jsonObject = JsonParser.parseString(json).getAsJsonObject();

            var message = jsonObject.get("message").getAsString();

            if (message.equals("params_error")) {
                throw new TikTokLiveRequestException("fetchRoomIdFromTiktokApi -> Unable to fetch roomID, contact the developer");
            }
            if (message.equals("user_not_found")) {
                return new LiveUserData.Response(json, LiveUserData.UserStatus.NotFound, "", -1, null);
            }
            //live -> status 2
            //live paused -> 3
            //not live -> status 4
            var element = jsonObject.get("data");
            if (element.isJsonNull()) {
                return new LiveUserData.Response(json, LiveUserData.UserStatus.NotFound, "", -1, null);
            }
            var data = element.getAsJsonObject();
            var user = data.getAsJsonObject("user");
            var stats = data.getAsJsonObject("stats");
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

            User foundUser = new User(
                Long.parseLong(user.get("id").getAsString()),
                user.get("uniqueId").getAsString(),
                user.get("nickname").getAsString(),
                new Picture(user.get("avatarLarger").getAsString()),
                stats.get("followingCount").getAsLong(),
                stats.get("followerCount").getAsLong(),
                List.of());

            return new LiveUserData.Response(json, statusEnum, roomId, startTime, foundUser);
        } catch (JsonSyntaxException | IllegalStateException e) {
            logger.warning("Malformed Json: '"+json+"' - Error Message: "+e.getMessage());
            return new LiveUserData.Response(json, LiveUserData.UserStatus.NotFound, "", -1, null);
        }
    }
}