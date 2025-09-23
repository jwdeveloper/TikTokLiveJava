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
package io.github.jwdeveloper.tiktok.websocket;

import io.github.jwdeveloper.tiktok.data.dto.ProxyData;
import io.github.jwdeveloper.tiktok.data.requests.LiveConnectionData;
import io.github.jwdeveloper.tiktok.data.settings.*;
import io.github.jwdeveloper.tiktok.exceptions.*;
import io.github.jwdeveloper.tiktok.live.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;

import javax.net.ssl.*;
import java.net.Proxy;
import java.security.cert.X509Certificate;
import java.util.HashMap;

public class TikTokWebSocketClient implements LiveSocketClient {
    private final LiveClientSettings clientSettings;
    private final LiveMessagesHandler messageHandler;
    private final LiveEventsHandler tikTokEventHandler;
    private final WebSocketHeartbeatTask heartbeatTask;
    private WebSocketClient webSocketClient;

    public TikTokWebSocketClient(
            LiveClientSettings clientSettings,
            LiveMessagesHandler messageHandler,
            LiveEventsHandler tikTokEventHandler,
            WebSocketHeartbeatTask heartbeatTask)
    {
        this.clientSettings = clientSettings;
        this.messageHandler = messageHandler;
        this.tikTokEventHandler = tikTokEventHandler;
        this.heartbeatTask = heartbeatTask;
    }

    @Override
    public void start(LiveConnectionData.Response connectionData, LiveClient liveClient) {
        if (isConnected())
			stop(LiveClientStopType.NORMAL);

        messageHandler.handle(liveClient, connectionData.getWebcastResponse());

        var headers = new HashMap<>(clientSettings.getHttpSettings().getHeaders());
        headers.put("Cookie", connectionData.getWebsocketCookies());
        webSocketClient = new TikTokWebSocketListener(connectionData.getWebsocketUrl(),
            headers,
            clientSettings.getHttpSettings().getTimeout().toMillisPart(),
            messageHandler,
            tikTokEventHandler,
            liveClient);

        ProxyClientSettings proxyClientSettings = clientSettings.getHttpSettings().getProxyClientSettings();
        if (proxyClientSettings.isEnabled() && proxyClientSettings.isAllowWebsocket())
            connectProxy(proxyClientSettings);
        else
            connectDefault();
    }

    public void connectDefault() {
        try {
            webSocketClient.connect();
            heartbeatTask.run(webSocketClient, clientSettings.getPingInterval());
        } catch (Exception e) {
            throw new TikTokLiveException("Failed to connect to the websocket", e);
        }
    }

    public void connectProxy(ProxyClientSettings proxySettings) {
        try {
            if (proxySettings.getType() == Proxy.Type.SOCKS) {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, new TrustManager[]{new X509TrustManager() {
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) {
                    }

                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) {
                    }

                    public X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }
                }}, null);
                webSocketClient.setSocketFactory(sc.getSocketFactory());
            }
        } catch (Exception e) {
            // This will never be thrown.
            throw new TikTokProxyRequestException("Unable to set Socks proxy SSL instance");
        }
        while (proxySettings.hasNext()) {
            ProxyData proxyData = proxySettings.next();
			if (tryProxyConnection(proxySettings, proxyData)) {
				heartbeatTask.run(webSocketClient, clientSettings.getPingInterval());
				return;
			}
            if (proxySettings.isAutoDiscard())
                proxySettings.remove();
		}
        throw new TikTokLiveException("Failed to connect to the websocket");
    }

    public boolean tryProxyConnection(ProxyClientSettings proxySettings, ProxyData proxyData) {
        try {
            webSocketClient.setProxy(new Proxy(proxySettings.getType(), proxyData.toSocketAddress()));
            webSocketClient.connect();
            return true;
        } catch (Exception e) {
            return false;
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
            heartbeatTask.stop();
        }
        webSocketClient = null;
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }
}