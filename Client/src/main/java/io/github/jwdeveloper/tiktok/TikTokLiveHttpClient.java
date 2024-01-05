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
package io.github.jwdeveloper.tiktok;

import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.data.requests.*;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokSignServerException;
import io.github.jwdeveloper.tiktok.http.HttpClientFactory;
import io.github.jwdeveloper.tiktok.http.LiveHttpClient;
import io.github.jwdeveloper.tiktok.http.mappers.GiftsDataMapper;
import io.github.jwdeveloper.tiktok.http.mappers.LiveDataMapper;
import io.github.jwdeveloper.tiktok.http.mappers.LiveUserDataMapper;
import io.github.jwdeveloper.tiktok.http.mappers.SignServerResponseMapper;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;

import java.net.http.HttpResponse;

public class TikTokLiveHttpClient implements LiveHttpClient {

    /**
     * Signing API by Isaac Kogan
     * https://github-wiki-see.page/m/isaackogan/TikTokLive/wiki/All-About-Signatures
     */
    private static final String TIKTOK_SIGN_API = "https://tiktok.eulerstream.com/webcast/sign_url";
    private static final String TIKTOK_URL_WEB = "https://www.tiktok.com/";
    private static final String TIKTOK_URL_WEBCAST = "https://webcast.tiktok.com/webcast/";

    private final HttpClientFactory httpFactory;
    private final LiveUserDataMapper liveUserDataMapper;
    private final LiveDataMapper liveDataMapper;
    private final SignServerResponseMapper signServerResponseMapper;
    private final GiftsDataMapper giftsDataMapper;

    public TikTokLiveHttpClient(HttpClientFactory factory) {
        this.httpFactory = factory;
        liveUserDataMapper = new LiveUserDataMapper();
        liveDataMapper = new LiveDataMapper();
        signServerResponseMapper = new SignServerResponseMapper();
        giftsDataMapper = new GiftsDataMapper();
    }

    public TikTokLiveHttpClient() {
        this(new HttpClientFactory(LiveClientSettings.createDefault()));
    }


    public GiftsData.Response fetchGiftsData() {
        var url = TIKTOK_URL_WEBCAST + "gift/list/";
        var optional = httpFactory.client(url)
                .build()
                .toJsonResponse();

        if (optional.isEmpty()) {
            throw new TikTokLiveRequestException("Unable to fetch gifts information's");
        }
        var json = optional.get();
        return giftsDataMapper.map(json);
    }


    @Override
    public LiveUserData.Response fetchLiveUserData(String userName) {
        return fetchLiveUserData(new LiveUserData.Request(userName));
    }

    @Override
    public LiveUserData.Response fetchLiveUserData(LiveUserData.Request request) {

        var url = TIKTOK_URL_WEB + "api-live/user/room";
        var optional = httpFactory.client(url)
                .withParam("uniqueId", request.getUserName())
                .withParam("sourceType", "54")
                .build()
                .toJsonResponse();

        if (optional.isEmpty()) {
            throw new TikTokLiveRequestException("Unable to get information's about user");
        }

        var json = optional.get();
        return liveUserDataMapper.map(json);
    }

    @Override
    public LiveData.Response fetchLiveData(String roomId) {
        return fetchLiveData(new LiveData.Request(roomId));
    }

    @Override
    public LiveData.Response fetchLiveData(LiveData.Request request) {

        var url = TIKTOK_URL_WEBCAST + "room/info";
        var optional = httpFactory.client(url)
                .withParam("room_id", request.getRoomId())
                .build()
                .toJsonResponse();

        if (optional.isEmpty()) {
            throw new TikTokLiveRequestException("Unable to get info about live room");
        }

        var json = optional.get();
        return liveDataMapper.map(json);
    }

    @Override
    public LiveConnectionData.Response fetchLiveConnectionData(String roomId) {
        return fetchLiveConnectionData(new LiveConnectionData.Request(roomId));
    }

    @Override
    public LiveConnectionData.Response fetchLiveConnectionData(LiveConnectionData.Request request) {

        var signServerResponse = getSignedUrl(request.getRoomId());
        var credentialsResponse = getWebsocketCredentialsResponse(signServerResponse.getSignedUrl());

        try {
            var optionalHeader = credentialsResponse.headers().firstValue("set-cookie");
            if (optionalHeader.isEmpty()) {
                throw new TikTokSignServerException("Sign server does not returned set-cookie header");
            }
            var websocketCookie = optionalHeader.get();
            var webcastResponse = WebcastResponse.parseFrom(credentialsResponse.body());
            var webSocketUrl = httpFactory
                    .client(webcastResponse.getPushServer())
                    .withParam("room_id", request.getRoomId())
                    .withParam("cursor", webcastResponse.getCursor())
                    .withParam("resp_content_type", "protobuf")
                    .withParam("internal_ext", webcastResponse.getInternalExt())
                    .withParams(webcastResponse.getRouteParamsMapMap())
                    .build()
                    .toUrl();

            return new LiveConnectionData.Response(websocketCookie, webSocketUrl, webcastResponse);
        } catch (InvalidProtocolBufferException e) {
            throw new TikTokSignServerException("Unable to parse websocket credentials response to WebcastResponse");
        }
    }

    SignServerResponse getSignedUrl(String roomId) {
        var urlToSign = httpFactory
                .client(TikTokLiveHttpClient.TIKTOK_URL_WEBCAST + "im/fetch")
                .withParam("room_id", roomId)
                .build()
                .toUrl();


        var optional = httpFactory
                .client(TikTokLiveHttpClient.TIKTOK_SIGN_API)
                .withParam("client", "ttlive-java")
                .withParam("uuc", "1")
                .withParam("url", urlToSign.toString())
                .build()
                .toJsonResponse();

        if (optional.isEmpty()) {
            throw new TikTokSignServerException("Unable to sign url: " + urlToSign);
        }

        var json = optional.get();
        return signServerResponseMapper.map(json);
    }

    HttpResponse<byte[]> getWebsocketCredentialsResponse(String signedUrl) {
        var optionalResponse = httpFactory
                .clientEmpty(signedUrl)
                .build()
                .toResponse(HttpResponse.BodyHandlers.ofByteArray());
        if (optionalResponse.isEmpty()) {
            throw new TikTokSignServerException("Unable to get websocket connection credentials");
        }
        return optionalResponse.get();
    }

}