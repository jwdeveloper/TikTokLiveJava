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
package io.github.jwdeveloper.tiktok.websocket;


import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.TikTokLiveEventHandler;
import io.github.jwdeveloper.tiktok.TikTokLiveMessageHandler;
import io.github.jwdeveloper.tiktok.data.requests.LiveConnectionData;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import org.java_websocket.client.WebSocketClient;

import java.util.HashMap;

public class TikTokWebSocketClient implements SocketClient {
    private final LiveClientSettings clientSettings;
    private final TikTokLiveMessageHandler messageHandler;
    private final TikTokLiveEventHandler tikTokEventHandler;
    private WebSocketClient webSocketClient;
    private boolean isConnected;

    public TikTokWebSocketClient(
            LiveClientSettings clientSettings,
            TikTokLiveMessageHandler messageHandler,
            TikTokLiveEventHandler tikTokEventHandler) {
        this.clientSettings = clientSettings;
        this.messageHandler = messageHandler;
        this.tikTokEventHandler = tikTokEventHandler;
        isConnected = false;
    }
    @Override
    public void start(LiveConnectionData.Response connectionData, LiveClient liveClient)
    {

        if (isConnected) {
            stop();
        }

        messageHandler.handle(liveClient, connectionData.getWebcastResponse());

        var headers = new HashMap<String, String>();
        headers.put("Cookie", connectionData.getWebsocketCookies());
        webSocketClient = new TikTokWebSocketListener(connectionData.getWebsocketUrl(),
                headers,
                clientSettings.getHttpSettings().getTimeout().toMillisPart(),
                messageHandler,
                tikTokEventHandler,
                liveClient);

        try
        {
            webSocketClient.connect();
            isConnected = true;
        } catch (Exception e)
        {
            isConnected = false;
            throw new TikTokLiveException("Failed to connect to the websocket", e);
        }
    }





    public void stop() {
        if (isConnected && webSocketClient != null) {
            webSocketClient.closeConnection(0, "");
        }
        webSocketClient = null;
        isConnected = false;
    }
}
