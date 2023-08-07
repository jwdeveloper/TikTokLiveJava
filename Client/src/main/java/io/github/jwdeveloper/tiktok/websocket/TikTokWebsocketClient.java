package io.github.jwdeveloper.tiktok.websocket;

import io.github.jwdeveloper.generated.WebcastResponse;
import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.Constants;
import io.github.jwdeveloper.tiktok.TikTokLiveException;
import io.github.jwdeveloper.tiktok.http.HttpUtils;
import io.github.jwdeveloper.tiktok.http.TikTokCookieJar;
import io.github.jwdeveloper.tiktok.http.TikTokHttpRequestFactory;

import java.net.URI;
import java.net.http.HttpClient;
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

    public TikTokWebsocketClient(Logger logger,
                                 TikTokCookieJar tikTokCookieJar,
                                 Map<String, Object> clientParams,
                                 TikTokHttpRequestFactory factory,
                                 ClientSettings clientSettings) {
        this.logger = logger;
        this.clientParams = clientParams;
        this.tikTokCookieJar = tikTokCookieJar;
        this.clientSettings = clientSettings;
        this.factory = factory;
    }

    public void start(WebcastResponse webcastResponse) {
        if (webcastResponse.getWsUrl().isEmpty() || webcastResponse.getWsParam().getAllFields().isEmpty()) {
            throw new TikTokLiveException("Could not find Room");
        }


        try {

            var params = webcastResponse.getWsParam();
            var name = params.getName();
            var value = params.getValue();
            System.out.println("KEY: " + name + " value: " + value);


            var headers = Constants.DefaultRequestHeaders();


            var clone = new TreeMap<>(clientParams);
            //  clone.putAll(headers);
            clone.put(name, value);
            clone.put("compress", "gzip");

            var url = webcastResponse.getWsUrl();
            var wsUrl = HttpUtils.parseParametersEncode(url, clone);
            logger.info("Starting Socket-Threads");
            //runningTask = Task.Run(WebSocketLoop, token);
            //pollingTask = Task.Run(PingLoop, token);
            startWS(wsUrl);
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

            var ws =  factory.openSocket()
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

    public void stop() {

    }
}
