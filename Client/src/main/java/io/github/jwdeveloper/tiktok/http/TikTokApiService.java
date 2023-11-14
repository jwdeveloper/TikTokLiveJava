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
package io.github.jwdeveloper.tiktok.http;

import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.data.dto.TikTokUserInfo;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveOfflineHostException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.live.LiveRoomMeta;
import io.github.jwdeveloper.tiktok.mappers.LiveRoomMetaMapper;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;

import java.util.HashMap;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class TikTokApiService {
    private final TikTokHttpClient tiktokHttpClient;
    private final Logger logger;
    private final ClientSettings clientSettings;

    public TikTokApiService(TikTokHttpClient apiClient, Logger logger, ClientSettings clientSettings) {
        this.tiktokHttpClient = apiClient;
        this.logger = logger;
        this.clientSettings = clientSettings;
    }


    public void updateSessionId() {
        if (clientSettings.getSessionId() == null) {
            return;
        }
        if (clientSettings.getSessionId().isEmpty()) {
            return;
        }
        tiktokHttpClient.setSessionId(clientSettings.getSessionId());
    }

    public String fetchRoomId(String userName) {
        var userInfo = fetchUserInfoFromTikTokApi(userName);
        clientSettings.getClientParameters().put("room_id", userInfo.getRoomId());
        logger.info("RoomID -> " + userInfo.getRoomId());
        return userInfo.getRoomId();
    }

    public TikTokUserInfo fetchUserInfoFromTikTokApi(String userName) {

        var params = new HashMap<>(clientSettings.getClientParameters());
        params.put("uniqueId", userName);
        params.put("sourceType", 54);
        var roomData = tiktokHttpClient.getJsonFromTikTokApi("api-live/user/room/", params);

        var message = roomData.get("message").getAsString();

        if (message.equals("params_error")) {
            throw new TikTokLiveRequestException("fetchRoomIdFromTiktokApi -> Unable to fetch roomID, contact with developer");
        }
        if (message.equals("user_not_found")) {
            return new TikTokUserInfo(TikTokUserInfo.UserStatus.NotFound, "");
        }
        //live -> status 2
        //live paused -> 3
        //not live -> status 4
        var data = roomData.getAsJsonObject("data");
        var user = data.getAsJsonObject("user");
        var roomId = user.get("roomId").getAsString();
        var status = user.get("status").getAsInt();

        var statusEnum = switch (status) {
            case 2 ->  TikTokUserInfo.UserStatus.Live;
            case 3 -> TikTokUserInfo.UserStatus.LivePaused;
            case 4 -> TikTokUserInfo.UserStatus.Offline;
            default -> TikTokUserInfo.UserStatus.NotFound;
        };

        return new TikTokUserInfo(statusEnum, roomId);
    }


    public LiveRoomMeta fetchRoomInfo() {
        logger.info("Fetching RoomInfo");
        try {
            var response = tiktokHttpClient.getJsonFromWebcastApi("room/info/", clientSettings.getClientParameters());
            var mapper = new LiveRoomMetaMapper();
            var liveRoomMeta = mapper.map(response);
            logger.info("RoomInfo status -> " + liveRoomMeta.getStatus());
            return liveRoomMeta;
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Failed to fetch room info from WebCast, see stacktrace for more info.", e);
        }
    }

    public WebcastResponse fetchClientData() {

        logger.info("Fetching ClientData");
        try {
            var response = tiktokHttpClient.getSigningServerResponse("im/fetch/", clientSettings.getClientParameters());
            clientSettings.getClientParameters().put("cursor", response.getCursor());
            clientSettings.getClientParameters().put("internal_ext", response.getInternalExt());
            return response;
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Failed to fetch client data", e);
        }
    }
}
