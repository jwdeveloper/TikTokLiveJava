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
package io.github.jwdeveloper.tiktok.websocket.euler;

import io.github.jwdeveloper.tiktok.data.requests.LiveConnectionData;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.live.*;
import io.github.jwdeveloper.tiktok.websocket.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;

import java.net.URI;
import java.util.HashMap;

public class TikTokWebSocketEulerClient implements LiveSocketClient {

    private final LiveClientSettings clientSettings;
    private final LiveMessagesHandler messageHandler;
    private final LiveEventsHandler tikTokEventHandler;
    private WebSocketClient webSocketClient;

    public TikTokWebSocketEulerClient(
        LiveClientSettings clientSettings,
        LiveMessagesHandler messageHandler,
        LiveEventsHandler tikTokEventHandler)
    {
        this.clientSettings = clientSettings;
        this.messageHandler = messageHandler;
        this.tikTokEventHandler = tikTokEventHandler;
    }

    @Override
    public void start(LiveConnectionData.Response connectionData, LiveClient liveClient) {
        if (isConnected())
			stop(LiveClientStopType.NORMAL);

        String url = "wss://ws.eulerstream.com?uniqueId=%s&apiKey=%s&features.rawMessages=true".formatted(liveClient.getRoomInfo().getHostName(), clientSettings.getApiKey())
                     + (clientSettings.isUseEulerstreamWebsocket() ? "&features.useEnterpriseApi=true" : "");

        webSocketClient = new TikTokWebSocketEulerListener(
            URI.create(url),
            new HashMap<>(clientSettings.getHttpSettings().getHeaders()),
            clientSettings.getHttpSettings().getTimeout().toMillisPart(),
            messageHandler,
            tikTokEventHandler,
            liveClient);

        connect();
    }

    public void connect() {
        try {
            webSocketClient.connect();
        } catch (Exception e) {
            throw new TikTokLiveException("Failed to connect to the websocket", e);
        }
    }

    public void stop(LiveClientStopType type) {
        if (isConnected()) {
            switch (type) {
                case CLOSE_BLOCKING -> {
                    try {
                        webSocketClient.closeBlocking();
                    } catch (InterruptedException e) {
                        throw new TikTokLiveException("Failed to stop the websocket");
                    }
                }
                case DISCONNECT -> webSocketClient.closeConnection(CloseFrame.NORMAL, "");
                default -> webSocketClient.close();
            }
        }
        webSocketClient = null;
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }
}