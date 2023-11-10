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
package io.github.jwdeveloper.tiktok.mockClient;

import io.github.jwdeveloper.tiktok.TikTokLiveClientBuilder;
import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.gifts.TikTokGiftManager;
import io.github.jwdeveloper.tiktok.handlers.TikTokMessageHandlerRegistration;
import io.github.jwdeveloper.tiktok.handlers.events.TikTokGiftEventHandler;
import io.github.jwdeveloper.tiktok.http.TikTokCookieJar;
import io.github.jwdeveloper.tiktok.http.TikTokHttpClient;
import io.github.jwdeveloper.tiktok.http.TikTokHttpRequestFactory;
import io.github.jwdeveloper.tiktok.listener.TikTokListenersManager;
import io.github.jwdeveloper.tiktok.mappers.TikTokGenericEventMapper;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.mockClient.mocks.ApiServiceMock;
import io.github.jwdeveloper.tiktok.mockClient.mocks.LiveClientMock;
import io.github.jwdeveloper.tiktok.mockClient.mocks.WebsocketClientMock;

import java.util.Base64;
import java.util.List;
import java.util.Stack;


public class TikTokMockBuilder extends TikTokLiveClientBuilder {

    Stack<WebcastResponse> responses;

    public TikTokMockBuilder(String userName) {
        super(userName);
        responses = new Stack<>();

    }

    public TikTokMockBuilder addResponse(String value) {
        var bytes = Base64.getDecoder().decode(value);
        return addResponse(bytes);
    }

    public TikTokMockBuilder addResponses(List<String> values) {
        for (var value : values) {
            try {
                addResponse(value);
            } catch (Exception e) {
                throw new TikTokLiveException(value, e);
            }
        }
        return this;
    }

    public TikTokMockBuilder addResponse(byte[] bytes) {
        try {
            var response = WebcastResponse.parseFrom(bytes);
            return addResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse response from bytes", e);
        }
    }

    public TikTokMockBuilder addResponse(WebcastResponse message) {
        responses.push(message);
        return this;
    }


    @Override
    public LiveClientMock build() {
        validate();

        var cookie = new TikTokCookieJar();
        var tiktokRoomInfo = new TikTokRoomInfo();
        tiktokRoomInfo.setHostName(clientSettings.getHostName());

        var listenerManager = new TikTokListenersManager(listeners, tikTokEventHandler);
        var giftManager = new TikTokGiftManager(logger);
        var requestFactory = new TikTokHttpRequestFactory(cookie);
        var apiClient = new TikTokHttpClient(cookie, requestFactory);
        var apiService = new ApiServiceMock(apiClient, logger, clientSettings);
        var webResponseHandler = new TikTokMessageHandlerRegistration(tikTokEventHandler,
                tiktokRoomInfo,
                new TikTokGenericEventMapper(),
                new TikTokGiftEventHandler(giftManager));
        var webSocketClient = new WebsocketClientMock(logger, responses, webResponseHandler);

        return new LiveClientMock(tiktokRoomInfo,
                apiService,
                webSocketClient,
                giftManager,
                tikTokEventHandler,
                clientSettings,
                listenerManager,
                logger);
    }

    @Override
    public LiveClientMock buildAndConnect() {
        var client = build();
        client.connect();
        return client;
    }
}
