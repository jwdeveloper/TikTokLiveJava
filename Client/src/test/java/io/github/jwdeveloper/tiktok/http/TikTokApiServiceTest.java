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
package io.github.jwdeveloper.tiktok.http;

import com.google.gson.JsonObject;
import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.live.LiveRoomMeta;
import io.github.jwdeveloper.tiktok.mappers.LiveRoomMetaMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class TikTokApiServiceTest
{
    @Mock
    TikTokHttpClient tiktokHttpClient;

    @Mock
    Logger logger;

    @Mock
    ClientSettings clientSettings;

    @InjectMocks
    TikTokApiService tikTokApiService;

    @Test
    void updateSessionId_NullSessionId_DoesNotSetSessionId() {
        when(clientSettings.getSessionId()).thenReturn(null);

        tikTokApiService.updateSessionId();

        verify(tiktokHttpClient, times(0)).setSessionId(anyString());
    }

    @Test
    void updateSessionId_EmptySessionId_DoesNotSetSessionId() {
        when(clientSettings.getSessionId()).thenReturn("");

        tikTokApiService.updateSessionId();

        verify(tiktokHttpClient, times(0)).setSessionId(anyString());
    }

    @Test
    void updateSessionId_ValidSessionId_SetsSessionId() {
        when(clientSettings.getSessionId()).thenReturn("validSessionId");

        tikTokApiService.updateSessionId();

        verify(tiktokHttpClient, times(1)).setSessionId("validSessionId");
    }

    @Test
    void sendMessage_EmptySessionId_ThrowsException() {
        assertThrows(TikTokLiveException.class, () -> {
            tikTokApiService.sendMessage("some message", "");
        });
    }

    @Test
    void sendMessage_NullRoomId_ThrowsException() {
        when(clientSettings.getClientParameters()).thenReturn(new HashMap<>());

        assertThrows(TikTokLiveException.class, () -> {
            tikTokApiService.sendMessage("some message", "someSessionId");
        });
    }

    @Test
    void fetchRoomId_ValidResponse_ReturnsRoomId() throws Exception {
        String expectedRoomId = "123456";
        String htmlResponse = "room_id=" + expectedRoomId ;
        when(tiktokHttpClient.getLivestreamPage(anyString())).thenReturn(htmlResponse);

        String roomId = tikTokApiService.fetchRoomId("username");

        assertEquals(expectedRoomId, roomId);
        verify(clientSettings.getClientParameters()).put("room_id", expectedRoomId);
    }

    @Test
    void fetchRoomId_ExceptionThrown_ThrowsTikTokLiveRequestException() throws Exception {
        when(tiktokHttpClient.getLivestreamPage(anyString())).thenThrow(new Exception("some exception"));

        assertThrows(TikTokLiveRequestException.class, () -> {
            tikTokApiService.fetchRoomId("username");
        });
    }

    @Test
    void fetchRoomInfo_ValidResponse_ReturnsLiveRoomMeta() throws Exception {
        HashMap<String, Object> clientParameters = new HashMap<>();
        var mockResponse = new JsonObject();  // Assume JsonObject is from the Gson library
        var expectedLiveRoomMeta = new LiveRoomMeta();  // Assume LiveRoomMeta is a simple POJO

        when(clientSettings.getClientParameters()).thenReturn(clientParameters);
        when(tiktokHttpClient.getJObjectFromWebcastAPI(anyString(), any())).thenReturn(mockResponse);
        when(new LiveRoomMetaMapper().mapFrom(mockResponse)).thenReturn(expectedLiveRoomMeta);  // Assuming LiveRoomMetaMapper is a simple mapper class

        LiveRoomMeta liveRoomMeta = tikTokApiService.fetchRoomInfo();

        assertEquals(expectedLiveRoomMeta, liveRoomMeta);
    }

    @Test
    void fetchRoomInfo_ExceptionThrown_ThrowsTikTokLiveRequestException() throws Exception {
        when(tiktokHttpClient.getJObjectFromWebcastAPI(anyString(), any())).thenThrow(new Exception("some exception"));

        assertThrows(TikTokLiveRequestException.class, () -> {
            tikTokApiService.fetchRoomInfo();
        });
    }

}