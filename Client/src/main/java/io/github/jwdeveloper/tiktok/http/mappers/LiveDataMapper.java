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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jwdeveloper.tiktok.TikTokLiveHttpClient;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.models.users.UserAttribute;
import io.github.jwdeveloper.tiktok.data.requests.LiveData;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;

import java.util.ArrayList;

public class LiveDataMapper {
    /**
     * 0 - Unknown
     * 1 - ?
     * 2 - Online
     * 3 - ?
     * 4 - Offline
     */
    public LiveData.Response map(String json) {
        var response = new LiveData.Response();

        response.setJson(json);

        var parsedJson = JsonParser.parseString(json);
        var jsonObject = parsedJson.getAsJsonObject();


        if (!jsonObject.has("data")) {
            throw new TikTokLiveRequestException("Data section not found in LiveData.Response");
        }
        var data = jsonObject.getAsJsonObject("data");


        if (data.has("status")) {
            var status = data.get("status");
            var statusId = status.getAsInt();
            var statusValue = switch (statusId) {
                case 2 -> LiveData.LiveStatus.HostOnline;
                case 4 -> LiveData.LiveStatus.HostOffline;
                default -> LiveData.LiveStatus.HostNotFound;
            };
            response.setLiveStatus(statusValue);
        } else if (data.has("prompts") && data.get("prompts").getAsString().isEmpty() && jsonObject.has("status_code")) {
            response.setAgeRestricted(jsonObject.get("status_code").getAsInt() == TikTokLiveHttpClient.TIKTOK_AGE_RESTRICTED_CODE);
        } else {
            response.setLiveStatus(LiveData.LiveStatus.HostNotFound);
        }

        if (data.has("age_restricted")) {
            var element = data.getAsJsonObject("age_restricted");
            var restricted = element.get("restricted").getAsBoolean();
            response.setAgeRestricted(restricted);
        }

        if (data.has("title")) {
            var element = data.get("title");
            var title = element.getAsString();
            response.setTitle(title);
        }

        if (data.has("stats")) {
            var statsElement = data.getAsJsonObject("stats");
            var likeElement = statsElement.get("like_count");
            var likes = likeElement.getAsInt();

            var titalUsersElement = statsElement.get("total_user");
            var totalUsers = titalUsersElement.getAsInt();


            response.setLikes(likes);
            response.setTotalViewers(totalUsers);
        }

        if (data.has("user_count")) {
            var element = data.get("user_count");
            var viewers = element.getAsInt();
            response.setViewers(viewers);
        }

        if (data.has("owner")) {
            var element = data.getAsJsonObject("owner");
            var user = getUser(element);
            response.setHost(user);
        }

        if (data.has("link_mic")) {
            var element = data.getAsJsonObject("link_mic");
            var multi_live = element.get("multi_live_enum").getAsInt();
            var rival_id = element.get("rival_anchor_id").getAsInt();
            var battle_scores = element.get("battle_scores").getAsJsonArray();
            if (multi_live == 1) {
                if (!battle_scores.isEmpty())
                    response.setLiveType(LiveData.LiveType.BATTLE);
                else if (rival_id != 0)
                    response.setLiveType(LiveData.LiveType.CO_HOST);
                else
                    response.setLiveType(LiveData.LiveType.BOX);
            } else
                response.setLiveType(LiveData.LiveType.SOLO);
        }

        return response;
    }

    public User getUser(JsonObject jsonElement) {
        var id = jsonElement.get("id").getAsLong();
        var name = jsonElement.get("display_id").getAsString();
        var profileName = jsonElement.get("nickname").getAsString();


        var followElement = jsonElement.getAsJsonObject("follow_info");
        var followers = followElement.get("follower_count").getAsInt();
        var followingCount = followElement.get("following_count").getAsInt();


        var pictureElement = jsonElement.getAsJsonObject("avatar_large");
        var link = pictureElement.getAsJsonArray("url_list").get(1).getAsString();
        var picture = new Picture(link);

        var user = new User(id, name, profileName, picture, followingCount, followers, new ArrayList<>());
        user.addAttribute(UserAttribute.LiveHost);
        return user;
    }
}