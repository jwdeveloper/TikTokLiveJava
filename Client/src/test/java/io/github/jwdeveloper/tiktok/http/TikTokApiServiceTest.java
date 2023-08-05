package io.github.jwdeveloper.tiktok.http;

import com.google.gson.JsonParser;
import io.github.jwdeveloper.tiktok.live.models.gift.TikTokGift;
import org.java_websocket.WebSocket;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TikTokApiServiceTest {

    @Test
    void testFetchAvailableGifts() {
        // Arrange
        var mockApiClient = mock(TikTokHttpApiClient.class);
        var mockLogger = mock(Logger.class);
        var clientParams = new HashMap<String,Object>();
        var tikTokApiService = new TikTokApiService(mockApiClient, mockLogger, clientParams);

        var inputStream = getClass().getClassLoader().getResourceAsStream("gifts.json");
        String jsonContent;
        try (var scanner = new Scanner(inputStream, StandardCharsets.UTF_8.name())) {
            jsonContent = scanner.useDelimiter("\\A").next(); // Read entire content
        }
        var json = JsonParser.parseString(jsonContent);
        var jsonObject = json.getAsJsonObject();

        when(mockApiClient.GetJObjectFromWebcastAPI("gift/list/", clientParams))
                .thenReturn(jsonObject);

        var gifts = tikTokApiService.fetchAvailableGifts();

        assertNotNull(gifts);
    }


    @Test
    void test() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://tiktok.eulerstream.com/webcast/fetch/?room_id=7263690606554188577&client=ttlive-net&uuc=1&apiKey=&isSignRedirect=1&iph=658d90239052e48dabc4e5b61004661e"))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        System.out.println("Response code: " + response.statusCode());
        HttpHeaders headers = response.headers();
        headers.map().forEach((k, v) -> System.out.println(k + ":" + v));

        System.out.println("Response body: " + response.body());
    }


    @Test
    void testws2()
    {

        var url = "wss://webcast16-ws-useast1a.tiktok.com/webcast/im/push/?cursor=1691243226540_7263834340956643180_1_1_0_0&room_id=7263759223213132577&app_language=en-US&focus_state=true&last_rtt=0&did_rule=3&is_fullscreen=false&from_page=user&update_version_code=1.3.0&screen_height=1152&tz_name=Europe/Berlin&cookie_enabled=true&identity=audience&browser_platform=Win32&browser_version=5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/102.0.5005.63 Safari/537.36&browser_language=en&fetch_rule=1&value=u6Laa_b3czc3iEAb4x6oLXindKyTO&internal_ext=fetch_time:1691243226540|start_time:0|ack_ids:,|flag:0|seq:1|next_cursor:1691243226540_7263834340956643180_1_1_0_0|wss_info:0-1691243226540-0-0&screen_width=2048&version_code=180800&history_len=4&webcast_sdk_version=1.3.0&msToken=&app_name=tiktok_web&browser_name=Mozilla&resp_content_type=protobuf&live_id=12&webcast_language=en-US&name=imprp&device_platform=web&is_page_visible=true&aid=1988&browser_online=true";

        var split = url.substring(373,url.length()-1);

        var i =0;

        var uri = URI.create(url);



    }
}