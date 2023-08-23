package io.github.jwdeveloper.tiktok.websocket;


import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.Constants;
import io.github.jwdeveloper.tiktok.TikTokLiveClient;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.handlers.TikTokMessageHandlerRegistration;
import io.github.jwdeveloper.tiktok.http.HttpUtils;
import io.github.jwdeveloper.tiktok.http.TikTokCookieJar;
import io.github.jwdeveloper.tiktok.http.TikTokHttpRequestFactory;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;

import java.net.URI;
import java.net.http.WebSocket;
import java.time.Duration;
import java.util.HashMap;
import java.util.TreeMap;
import java.util.logging.Logger;

public class TikTokWebSocketClient {
    private final Logger logger;
    private final ClientSettings clientSettings;
    private final TikTokCookieJar tikTokCookieJar;
    private final TikTokHttpRequestFactory factory;
    private final TikTokMessageHandlerRegistration webResponseHandler;
    private final TikTokEventHandler tikTokEventHandler;

    private WebSocket webSocket;
    private boolean isConnected;
    private TikTokLiveClient tikTokLiveClient;

    public TikTokWebSocketClient(Logger logger,
                                 TikTokCookieJar tikTokCookieJar,
                                 TikTokHttpRequestFactory factory,
                                 ClientSettings clientSettings,
                                 TikTokMessageHandlerRegistration webResponseHandler,
                                 TikTokEventHandler tikTokEventHandler) {
        this.logger = logger;
        this.tikTokCookieJar = tikTokCookieJar;
        this.clientSettings = clientSettings;
        this.factory = factory;
        this.webResponseHandler = webResponseHandler;
        this.tikTokEventHandler = tikTokEventHandler;
        isConnected = false;
    }

    public void start(WebcastResponse webcastResponse, TikTokLiveClient tikTokLiveClient) {
        this.tikTokLiveClient = tikTokLiveClient;
        if (isConnected) {
            stop();
        }
        if (webcastResponse.getSocketUrl().isEmpty() || webcastResponse.getSocketParamsList().isEmpty()) {
            throw new TikTokLiveException("Could not find Room");
        }
        try {
            var url = getWebSocketUrl(webcastResponse);
            if (clientSettings.isHandleExistingMessagesOnConnect())
            {
                logger.info("Handling existing messages");
                webResponseHandler.handle(tikTokLiveClient, webcastResponse);
            }
            webSocket = startWebSocket(url);
        } catch (Exception e) {
            throw new TikTokLiveException("Failed to connect to the websocket", e);
        }
    }


    private String getWebSocketUrl(WebcastResponse webcastResponse) {
        var params = webcastResponse.getSocketParamsList().get(0);
        var name = params.getName();
        var value = params.getValue();
        var headers = Constants.DefaultRequestHeaders();


        var clone = new TreeMap<>(clientSettings.getClientParameters());
        clone.putAll(headers);
        clone.put(name, value);
        var url = webcastResponse.getSocketUrl();
        return HttpUtils.parseParametersEncode(url, clone);
    }

    private WebSocket startWebSocket(String url) throws Exception {
        var cookie = tikTokCookieJar.parseCookies();
        var map = new HashMap<String, String>();
        map.put("Cookie", cookie);
        return factory.openSocket()
                .subprotocols("echo-protocol")
                .connectTimeout(Duration.ofSeconds(15))
                .header("Cookie", cookie)
                .buildAsync(URI.create(url), new TikTokWebSocketListener(webResponseHandler, tikTokEventHandler, tikTokLiveClient)).get();
    }

    public void stop() {
        if (isConnected && webSocket != null) {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "ok");
        }
    }
}
