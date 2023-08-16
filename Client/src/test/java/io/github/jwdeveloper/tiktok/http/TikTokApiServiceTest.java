package io.github.jwdeveloper.tiktok.http;

import com.google.gson.JsonParser;
import org.junit.Test;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Scanner;
import java.util.logging.Logger;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TikTokApiServiceTest {

    @Test
   public void testFetchAvailableGifts() {
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





}