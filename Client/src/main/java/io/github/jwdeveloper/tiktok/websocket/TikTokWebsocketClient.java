package io.github.jwdeveloper.tiktok.websocket;


import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.Constants;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.handlers.WebResponseHandler;
import io.github.jwdeveloper.tiktok.http.HttpUtils;
import io.github.jwdeveloper.tiktok.http.TikTokCookieJar;
import io.github.jwdeveloper.tiktok.http.TikTokHttpRequestFactory;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;

import java.net.URI;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

public class TikTokWebsocketClient {
    private final Logger logger;
    private final Map<String, Object> clientParams;
    private final ClientSettings clientSettings;
    private final TikTokCookieJar tikTokCookieJar;
    private final TikTokHttpRequestFactory factory;
    private final WebResponseHandler webResponseHandler;
    private final TikTokEventHandler tikTokEventHandler;

    private WebSocket webSocket;

    private boolean isConnected;

    public TikTokWebsocketClient(Logger logger,
                                 TikTokCookieJar tikTokCookieJar,
                                 Map<String, Object> clientParams,
                                 TikTokHttpRequestFactory factory,
                                 ClientSettings clientSettings,
                                 WebResponseHandler webResponseHandler,
                                 TikTokEventHandler tikTokEventHandler) {
        this.logger = logger;
        this.clientParams = clientParams;
        this.tikTokCookieJar = tikTokCookieJar;
        this.clientSettings = clientSettings;
        this.factory = factory;
        this.webResponseHandler = webResponseHandler;
        this.tikTokEventHandler = tikTokEventHandler;
        isConnected = false;
    }

    public void start(WebcastResponse webcastResponse)
    {
        if(isConnected)
        {
            stop();
        }
        if (webcastResponse.getSocketUrl().isEmpty() || webcastResponse.getSocketParamsList().isEmpty()) {
            throw new TikTokLiveException("Could not find Room");
        }
        try {
            var url =getWebSocketUrl(webcastResponse);
            startWebSocket(url);
            if (clientSettings.isHandleExistingMessagesOnConnect()) {
                //  HandleWebcastMessages(webcastResponse);
            }
        } catch (Exception e) {
            throw new TikTokLiveException("Failed to connect to the websocket", e);
        }
    }


    private String getWebSocketUrl(WebcastResponse webcastResponse) {
        var params = webcastResponse.getSocketParamsList().get(0);
        var name = params.getName();
        var value = params.getValue();
        var headers = Constants.DefaultRequestHeaders();


        var clone = new TreeMap<>(clientParams);
        clone.putAll(headers);
        clone.put(name, value);
        var url = webcastResponse.getSocketUrl();
        return HttpUtils.parseParametersEncode(url, clone);
    }

    private WebSocket startWebSocket(String url) throws Exception {
        var cookie = tikTokCookieJar.parseCookies();
        //  System.out.println("WssIP: " + url);
        //  System.out.println("Cookie: " + cookie);

        var map = new HashMap<String, String>();
        map.put("Cookie", cookie);

       return factory.openSocket()
                .subprotocols("echo-protocol")
                .connectTimeout(Duration.ofSeconds(15))
                .header("Cookie", cookie)
                .buildAsync(URI.create(url), new TikTokWebSocketListener(webResponseHandler, tikTokEventHandler)).get();
    }


    public void stop() {
        if(isConnected && webSocket != null)
        {
            webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "ok");
        }
    }
}
