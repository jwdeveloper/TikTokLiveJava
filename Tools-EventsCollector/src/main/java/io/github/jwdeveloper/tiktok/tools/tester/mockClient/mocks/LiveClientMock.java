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
package io.github.jwdeveloper.tiktok.tools.tester.mockClient.mocks;

import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.TikTokLiveClient;
import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.gifts.TikTokGiftManager;
import io.github.jwdeveloper.tiktok.TikTokLiveEventHandler;
import io.github.jwdeveloper.tiktok.TikTokLiveHttpClient;
import io.github.jwdeveloper.tiktok.listener.TikTokListenersManager;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;

import java.util.logging.Logger;

public class LiveClientMock extends TikTokLiveClient {

    private final WebsocketClientMock websocketClientMock;

    public LiveClientMock(
            TikTokRoomInfo tikTokLiveMeta,
            TikTokLiveHttpClient httpClient,
            WebsocketClientMock webSocketClient,
            TikTokGiftManager tikTokGiftManager,
            TikTokLiveEventHandler tikTokEventHandler,
            LiveClientSettings clientSettings,
            TikTokListenersManager listenersManager,
            Logger logger) {
        super(
                tikTokLiveMeta,
                httpClient,
                webSocketClient,
                tikTokGiftManager,
                tikTokEventHandler,
                clientSettings,
                listenersManager,
                logger);
        this.websocketClientMock = webSocketClient;
        websocketClientMock.setClient(this);
    }


    public void publishMessage(String type, String base64) {
        websocketClientMock.addMessage(type, base64);
    }

    public void publishMessage(Class<?> clazz, String base64) {
        websocketClientMock.addMessage(clazz.getSimpleName(), base64);
    }

    public void publishResponse(String value) {
        websocketClientMock.addResponse(value);
    }

    public void publishResponse(byte[] bytes) {
        websocketClientMock.addResponse(bytes);
    }

    public void publishResponse(WebcastResponse message) {
        websocketClientMock.addResponse(message);
    }
}
