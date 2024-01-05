package io.github.jwdeveloper.tiktok.http.mappers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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

        var user = new User(id, name, profileName, picture, followers, followingCount, new ArrayList<>());
        user.addAttribute(UserAttribute.LiveHost);
        return user;
    }
}
