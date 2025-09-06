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
package io.github.jwdeveloper.tiktok;

import com.google.gson.JsonObject;
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.dependance.injector.api.annotations.Inject;
import io.github.jwdeveloper.tiktok.common.*;
import io.github.jwdeveloper.tiktok.data.requests.*;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.*;
import io.github.jwdeveloper.tiktok.http.*;
import io.github.jwdeveloper.tiktok.http.mappers.*;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import io.github.jwdeveloper.tiktok.messages.webcast.ProtoMessageFetchResult;

import java.net.http.*;
import java.util.function.Consumer;
import java.util.logging.Logger;

public class TikTokLiveHttpClient implements LiveHttpClient
{
    /**
	 * <a href="https://github-wiki-see.page/m/isaackogan/TikTokLive/wiki/All-About-Signatures">Signing API by Isaac Kogan</a>
	 */
    private static final String TIKTOK_SIGN_API = "https://tiktok.eulerstream.com/webcast/fetch";
    private static final String TIKTOK_CHAT_URL = "https://tiktok.eulerstream.com/webcast/chat";
    private static final String TIKTOK_URL_WEB = "https://www.tiktok.com/";
    private static final String TIKTOK_URL_WEBCAST = "https://webcast.tiktok.com/webcast/";
    private static final String TIKTOK_ROOM_GIFTS_URL = TIKTOK_URL_WEBCAST+"gift/list/";
    private static final String TIKTOK_ROOM_INFO_URL = TIKTOK_URL_WEBCAST + "room/info";
    public static final int TIKTOK_AGE_RESTRICTED_CODE = 4003110;

    private final HttpClientFactory httpFactory;
    private final LiveClientSettings clientSettings;
    private final LiveUserDataMapper liveUserDataMapper;
    private final LiveDataMapper liveDataMapper;
    private final GiftsDataMapper giftsDataMapper;
    private final Logger logger;

    @Inject
    public TikTokLiveHttpClient(HttpClientFactory factory) {
        this.httpFactory = factory;
        this.clientSettings = factory.getLiveClientSettings();
        this.logger = LoggerFactory.create("HttpClient-"+hashCode(), clientSettings);
        liveUserDataMapper = new LiveUserDataMapper();
        liveDataMapper = new LiveDataMapper();
        giftsDataMapper = new GiftsDataMapper();
    }

    public TikTokLiveHttpClient(Consumer<LiveClientSettings> consumer) {
        this(new HttpClientFactory(LiveClientSettings.createDefault()));
        consumer.accept(clientSettings);
    }

    public GiftsData.Response fetchRoomGiftsData(String room_id) {
        var proxyClientSettings = clientSettings.getHttpSettings().getProxyClientSettings();
        if (proxyClientSettings.isEnabled()) {
            while (proxyClientSettings.hasNext()) {
                try {
                    return getRoomGiftsData(room_id);
                } catch (TikTokProxyRequestException ignored) {}
            }
        }
        return getRoomGiftsData(room_id);
    }

    public GiftsData.Response getRoomGiftsData(String room_id) {
        var result = httpFactory.client(TIKTOK_ROOM_GIFTS_URL)
            .withParam("room_id", room_id)
            .build()
            .toJsonResponse();

        if (result.isFailure())
            throw new TikTokLiveRequestException("Unable to fetch gifts information's - "+result);

        var json = result.getContent();
        return giftsDataMapper.mapRoom(json);
    }

    @Override
    public LiveUserData.Response fetchLiveUserData(LiveUserData.Request request) {
        var proxyClientSettings = clientSettings.getHttpSettings().getProxyClientSettings();
        if (proxyClientSettings.isEnabled()) {
            while (proxyClientSettings.hasNext()) {
                try {
                    return getLiveUserData(request);
                } catch (TikTokProxyRequestException ignored) {}
            }
        }
        return getLiveUserData(request);
    }

    public LiveUserData.Response getLiveUserData(LiveUserData.Request request) {
        var url = TIKTOK_URL_WEB + "api-live/user/room";
        var result = httpFactory.client(url)
            .withParam("uniqueId", request.getUserName())
            .withParam("sourceType", "54") //MAGIC NUMBER, WHAT 54 means?
            .withCookie("sessionid", clientSettings.getSessionId())
            .withCookie("tt-target-idc", clientSettings.getTtTargetIdc())
            .build()
            .toJsonResponse();

        if (result.isFailure())
            throw new TikTokLiveRequestException("Unable to get information's about user - "+result);

        var json = result.getContent();
        return liveUserDataMapper.map(json, logger);
    }

    @Override
    public LiveData.Response fetchLiveData(LiveData.Request request) {
        var proxyClientSettings = clientSettings.getHttpSettings().getProxyClientSettings();
        if (proxyClientSettings.isEnabled()) {
            while (proxyClientSettings.hasNext()) {
                try {
                    return getLiveData(request);
                } catch (TikTokProxyRequestException ignored) {}
            }
        }
        return getLiveData(request);
    }

    public LiveData.Response getLiveData(LiveData.Request request) {
        var result = httpFactory.client(TIKTOK_ROOM_INFO_URL)
            .withParam("room_id", request.getRoomId())
            .withCookie("sessionid", clientSettings.getSessionId())
            .withCookie("tt-target-idc", clientSettings.getTtTargetIdc())
            .build()
            .toJsonResponse();

        if (result.isFailure())
            throw new TikTokLiveRequestException("Unable to get info about live room - "+result);

        var json = result.getContent();
        return liveDataMapper.map(json);
    }

    @Override
    public LiveConnectionData.Response fetchLiveConnectionData(LiveConnectionData.Request request) {
        var result = getStartingPayload(request);
        HttpResponse<byte[]> credentialsResponse = result.getContent(); // Always guaranteed to have response

        try {
            var resultHeader = ActionResult.of(credentialsResponse.headers().firstValue("x-set-tt-cookie"));
            if (resultHeader.isFailure()) {
                logger.warning("Sign Server Headers: "+request.getRoomId()+" - "+credentialsResponse.headers().map());
                throw new TikTokSignServerException("Sign server did not return the x-set-tt-cookie header - "+result);
            }
            var websocketCookie = resultHeader.getContent();
            var webcastResponse = ProtoMessageFetchResult.parseFrom(credentialsResponse.body());
            var webSocketUrl = httpFactory
                    .client(webcastResponse.getPushServer())
                    .withParam("room_id", request.getRoomId())
                    .withParam("cursor", webcastResponse.getCursor())
                    .withParam("resp_content_type", "protobuf")
                    .withParam("internal_ext", webcastResponse.getInternalExt())
                    .withParams(webcastResponse.getRouteParamsMapMap())
                    .build()
                    .toUri();

            return new LiveConnectionData.Response(websocketCookie, webSocketUrl, webcastResponse);
        } catch (InvalidProtocolBufferException e) {
            throw new TikTokSignServerException("Unable to parse websocket credentials response to WebcastResponse - "+result);
        }
    }

    @Override
    public boolean sendChat(LiveRoomInfo roomInfo, String content) {
        var proxyClientSettings = clientSettings.getHttpSettings().getProxyClientSettings();
        if (proxyClientSettings.isEnabled()) {
            while (proxyClientSettings.hasNext()) {
                try {
                    return requestSendChat(roomInfo, content);
                } catch (TikTokProxyRequestException ignored) {}
            }
        }
        return requestSendChat(roomInfo, content);
    }

    public boolean requestSendChat(LiveRoomInfo roomInfo, String content) {
        JsonObject body = new JsonObject();
        body.addProperty("content", content);
        body.addProperty("sessionId", clientSettings.getSessionId());
        body.addProperty("ttTargetIdc", clientSettings.getTtTargetIdc());
        body.addProperty("roomId", roomInfo.getRoomId());
        var result = httpFactory.client(TIKTOK_CHAT_URL)
            .withHeader("Content-Type", "application/json")
            .withHeader("apiKey", clientSettings.getApiKey())
            .withBody(HttpRequest.BodyPublishers.ofString(body.toString()))
            .build()
            .toJsonResponse();
        return result.isSuccess();
    }

    protected ActionResult<HttpResponse<byte[]>> getStartingPayload(LiveConnectionData.Request request) {
        var proxyClientSettings = clientSettings.getHttpSettings().getProxyClientSettings();
        if (proxyClientSettings.isEnabled()) {
            while (proxyClientSettings.hasNext()) {
                try {
                    return getByteResponse(request.getRoomId());
                } catch (TikTokProxyRequestException | TikTokSignServerException ignored) {}
            }
        }
        return getByteResponse(request.getRoomId());
    }

    protected ActionResult<HttpResponse<byte[]>> getByteResponse(String room_id) {
        HttpClientBuilder builder = httpFactory.client(TIKTOK_SIGN_API)
            .withParam("client", "ttlive-java")
            .withParam("room_id", room_id);

        if (clientSettings.getSessionId() != null) // Allows receiving of all comments and Subscribe Events
            builder.withParam("session_id", clientSettings.getSessionId());
        if (clientSettings.getApiKey() != null)
            builder.withParam("apiKey", clientSettings.getApiKey());

        var result = builder.build().toHttpResponse(HttpResponse.BodyHandlers.ofByteArray());

        if (result.isFailure())
            throw new TikTokSignServerException("Unable to get websocket connection credentials - "+result);

        return result;
    }
}
