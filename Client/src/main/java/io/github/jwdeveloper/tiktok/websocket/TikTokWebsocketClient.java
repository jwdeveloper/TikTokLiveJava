package io.github.jwdeveloper.tiktok.websocket;

import io.github.jwdeveloper.generated.WebcastResponse;
import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.TikTokLiveException;
import io.github.jwdeveloper.tiktok.http.HttpUtils;

import java.net.URI;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.logging.Logger;

public class TikTokWebsocketClient {
    private final Logger logger;
    private final Map<String, Object> clientParams;
    private final ClientSettings clientSettings;

    public TikTokWebsocketClient(Logger logger, Map<String, Object> clientParams, ClientSettings clientSettings) {
        this.logger = logger;
        this.clientParams = clientParams;
        this.clientSettings = clientSettings;
    }

    public void start(WebcastResponse webcastResponse) {
        if (webcastResponse.getWsUrl().isEmpty() || webcastResponse.getWsParam().getAllFields().isEmpty()) {
            throw new TikTokLiveException("Could not find Room");
        }
        try {
            for (var param : webcastResponse.getWsParam().getAllFields().entrySet()) {
                var name = param.getKey().getName();
                var value = param.getValue();
                clientParams.put(name, value);
                logger.info("Adding Custom Param" + param.getKey().getName() + " " + param.getValue());
            }


            var url = webcastResponse.getWsUrl();
            var wsUrl = HttpUtils.parseParametersEncode(url, clientParams);
            logger.info("Creating Socket with URL " + wsUrl);
            //socketClient = new TikTokWebSocket(TikTokHttpRequest.CookieJar, token, settings.SocketBufferSize);
            //connectedSocketUrl = url;
            //await socketClient.Connect(url);

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

    public void startWS(String url)
    {
        try {

           var cookie = "tt_csrf_token=Fh92faHZ-fVnWZ8CG58Wb_kIC1hb-QzizkRM;ttwid=1%7CergNdYee4w-v_96VkhyDxkJ8NIavveA-NvCEdWF68Ik%7C1691076950%7C154533521f698b079ff5300fbd058e85e81a8ef64c41349f1d218124aa74a6db;";
          //  var url = "wss://webcast16-ws-useast1a.tiktok.com/webcast/im/push/?aid=1988&app_language=en-US&app_name=tiktok_web&browser_language=en&browser_name=Mozilla&browser_online=True&browser_platform=Win32&browser_version=5.0+(Windows+NT+10.0%3b+Win64%3b+x64)+AppleWebKit%2f537.36+(KHTML%2c+like+Gecko)+Chrome%2f102.0.5005.63+Safari%2f537.36&cookie_enabled=True&cursor=1691242057374_7263829320139870326_1_1_0_0&internal_ext=fetch_time%3a1691242057374%7cstart_time%3a0%7cack_ids%3a%2c%7cflag%3a0%7cseq%3a1%7cnext_cursor%3a1691242057374_7263829320139870326_1_1_0_0%7cwss_info%3a0-1691242057374-0-0&device_platform=web&focus_state=True&from_page=user&history_len=4&is_fullscreen=False&is_page_visible=True&did_rule=3&fetch_rule=1&identity=audience&last_rtt=0&live_id=12&resp_content_type=protobuf&screen_height=1152&screen_width=2048&tz_name=Europe%2fBerlin&referer=https%2c+%2f%2fwww.tiktok.com%2f&root_referer=https%2c+%2f%2fwww.tiktok.com%2f&msToken=&version_code=180800&webcast_sdk_version=1.3.0&update_version_code=1.3.0&webcast_language=en-US&room_id=7263759223213132577&imprp=u65Ja_b3czc3iEAb4x6oLXindKyTO";
            HttpClient client = HttpClient.newHttpClient();
            var ws = client.newWebSocketBuilder()
                    .subprotocols("echo-protocol")
                    .connectTimeout(Duration.ofSeconds(15))
                     .header("cookie",cookie)
                    .buildAsync(URI.create(url),new TikTokWebSocketListener()).get();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void stop() {

    }
}
