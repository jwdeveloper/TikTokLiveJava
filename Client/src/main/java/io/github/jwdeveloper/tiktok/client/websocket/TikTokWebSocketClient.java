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
package io.github.jwdeveloper.tiktok.client.websocket;

import io.github.jwdeveloper.tiktok.api.data.dto.ProxyData;
import io.github.jwdeveloper.tiktok.api.data.requests.LiveConnectionData;
import io.github.jwdeveloper.tiktok.api.data.settings.*;
import io.github.jwdeveloper.tiktok.api.exceptions.*;
import io.github.jwdeveloper.tiktok.api.live.*;
import io.github.jwdeveloper.tiktok.api.websocket.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.framing.CloseFrame;

import javax.net.ssl.*;
import java.net.Proxy;
import java.security.cert.X509Certificate;
import java.util.HashMap;

public class TikTokWebSocketClient implements LiveSocketClient
{
    private final LiveClientSettings clientSettings;
    private final LiveMessagesHandler messageHandler;
    private final LiveEventsHandler tikTokEventHandler;
    private final WebSocketHeartbeatTask heartbeatTask;
    private volatile WebSocketClient webSocketClient;

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
        stop(LiveClientStopType.NORMAL);

        messageHandler.handle(liveClient, connectionData.getWebcastResponse());

        var headers = new HashMap<>(clientSettings.getHttpSettings().getHeaders());
        headers.put("Cookie", connectionData.getWebsocketCookies());
        var socket = new TikTokWebSocketListener(connectionData.getWebsocketUrl(),
            headers,
            clientSettings.getHttpSettings().getTimeout().toMillisPart(),
            messageHandler,
            tikTokEventHandler,
            liveClient,
            this);
        synchronized (this) {
            webSocketClient = socket;
        }

        ProxyClientSettings proxyClientSettings = clientSettings.getHttpSettings().getProxyClientSettings();
        try {
            if (proxyClientSettings.isEnabled() && proxyClientSettings.isAllowWebsocket())
                connectProxy(proxyClientSettings);
            else
                connectDefault();
        } catch (RuntimeException e) {
            stop(LiveClientStopType.NORMAL);
            throw e;
        }
    }

    public void connectDefault() {
        try {
            webSocketClient.connect();
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
				return;
			}
            if (proxySettings.isAutoDiscard())
                proxySettings.remove(proxyData);
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
        WebSocketClient socket;
        synchronized (this) {
            heartbeatTask.stop();
            socket = webSocketClient;
            webSocketClient = null;
        }
        if (socket != null && !socket.isClosed()) {
            // No blocking wait for a handshake that has not completed.
            if (!socket.isOpen()) {
                socket.close(CloseFrame.NORMAL, "");
                return;
            }
            switch (type) {
                case CLOSE_BLOCKING -> {
					try {
						socket.closeBlocking();
					} catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new TikTokLiveException("Failed to stop the websocket", e);
                    }
				}
                case DISCONNECT -> socket.closeConnection(CloseFrame.NORMAL, "");
                default -> socket.close();
            }
        }
    }

    synchronized boolean onOpen(WebSocketClient socket) {
        if (webSocketClient != socket)
            return false;
        heartbeatTask.run(socket, clientSettings.getPingInterval());
        return true;
    }

    synchronized boolean onClose(WebSocketClient socket, LiveClient liveClient) {
        heartbeatTask.stop(socket);
        if (webSocketClient == null)
            return true;
        if (webSocketClient != socket)
            return false;
        webSocketClient = null;
        // Detach first so disconnect does not try to close from within onClose.
        liveClient.disconnect();
        return true;
    }

    synchronized void onError(WebSocketClient socket) {
        if (!socket.isOpen())
            heartbeatTask.stop(socket);
    }

    public boolean isConnected() {
        var socket = webSocketClient;
        return socket != null && socket.isOpen();
    }
}