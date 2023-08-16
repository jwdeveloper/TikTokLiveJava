package io.github.jwdeveloper.tiktok.websocket;


import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.Constants;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.handlers.WebResponseHandlerBase;
import io.github.jwdeveloper.tiktok.http.HttpUtils;
import io.github.jwdeveloper.tiktok.http.TikTokCookieJar;
import io.github.jwdeveloper.tiktok.http.TikTokHttpRequestFactory;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;
import org.java_websocket.drafts.Draft_6455;

import java.net.URI;
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
    private final WebResponseHandlerBase webResponseHandler;

    public TikTokWebsocketClient(Logger logger,
                                 TikTokCookieJar tikTokCookieJar,
                                 Map<String, Object> clientParams,
                                 TikTokHttpRequestFactory factory,
                                 ClientSettings clientSettings,
                                 WebResponseHandlerBase webResponseHandler) {
        this.logger = logger;
        this.clientParams = clientParams;
        this.tikTokCookieJar = tikTokCookieJar;
        this.clientSettings = clientSettings;
        this.factory = factory;
        this.webResponseHandler = webResponseHandler;
    }

    public void start(WebcastResponse webcastResponse) {
        if (webcastResponse.getSocketUrl().isEmpty() || webcastResponse.getSocketParamsList().isEmpty()) {
            throw new TikTokLiveException("Could not find Room");
        }
        try {

            var params = webcastResponse.getSocketParamsList().get(0);
            var name = params.getName();
            var value = params.getValue();
           // System.out.println("KEY: " + name + " value: " + value);


            var headers = Constants.DefaultRequestHeaders();


            var clone = new TreeMap<>(clientParams);
            clone.putAll(headers);
            clone.put(name, value);
            //clone.put("compress", "gzip");
            var url = webcastResponse.getSocketUrl();
            var wsUrl = HttpUtils.parseParametersEncode(url, clone);
            logger.info("Starting Socket-Threads");
            //runningTask = Task.Run(WebSocketLoop, token);
            //pollingTask = Task.Run(PingLoop, token);
            startWS2(wsUrl);
        } catch (Exception e) {
            throw new TikTokLiveException("Failed to connect to the websocket", e);
        }
        if (clientSettings.isHandleExistingMessagesOnConnect()) {
            try {
                //  HandleWebcastMessages(webcastResponse);
            } catch (Exception e) {
                throw new TikTokLiveException("Error Handling Initial Messages", e);
            }
        }
    }

    public void startWS(String url) {
        try {
            var cookie = tikTokCookieJar.parseCookies();
            System.out.println("WssIP: " + url);
            System.out.println("Cookie: " + cookie);

            var map = new HashMap<String, String>();
            map.put("Cookie", cookie);

            var ws = factory.openSocket()
                    .subprotocols("echo-protocol")
                    .connectTimeout(Duration.ofSeconds(15))
                    .header("Cookie", cookie)
                    .buildAsync(URI.create(url), new TikTokWebSocketListener()).get();


            while (true) {
                byte[] message = new byte[]{58, 2, 104, 98};
                ByteBuffer buffer = ByteBuffer.wrap(message);
                while (buffer.hasRemaining()) {
                    ws.sendPing(buffer);
                }
                buffer.clear();
                Thread.sleep(10);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void startWS2(String url) {
        try {
            var cookie = tikTokCookieJar.parseCookies();
            System.out.println("WssIP: " + url);
            System.out.println("Cookie: " + cookie);

            var map = new HashMap<String, String>();
            map.put("Cookie", cookie);

            var client = new WebSocketClientTest(URI.create(url), new Draft_6455(), map, 1500,webResponseHandler);
            client.connect();
              /*
            while (true) {
                byte[] message = new byte[]{58, 2, 104, 98};
                ByteBuffer buffer = ByteBuffer.wrap(message);
                while (buffer.hasRemaining()) {
                   // client.send(buffer);
                    client.sendPing();
                }
                buffer.clear();
                Thread.sleep(10);
            }*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {

    }
}
