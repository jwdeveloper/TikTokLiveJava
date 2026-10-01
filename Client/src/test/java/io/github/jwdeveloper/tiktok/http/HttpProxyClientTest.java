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
package io.github.jwdeveloper.tiktok.http;

import io.github.jwdeveloper.tiktok.data.dto.ProxyData;
import io.github.jwdeveloper.tiktok.data.settings.HttpClientSettings;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokProxyRequestException;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class HttpProxyClientTest {
    @Test
    void rotatesBeyondProxyCountUntilResponseSucceeds() throws Exception {
        var settings = settings(2);
        var proxies = settings.getProxyClientSettings();
        proxies.setAutoDiscard(false);
        var endpoints = proxies.getProxiesSnapshot();
        var selected = new ArrayList<ProxyData>();
        proxies.setOnProxyUpdated(selected::add);
        var transport = Mockito.mock(java.net.http.HttpClient.class);
        var unavailable = response(503);
        var rateLimited = response(429);
        var blocked = response(420);
        var serverFailure = response(500);
        var notFound = response(404);
        var success = response(200);
        Mockito.when(transport.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
            .thenReturn(unavailable, rateLimited, blocked, serverFailure, notFound, success);

        var result = client(settings, transport).toHttpResponse(HttpResponse.BodyHandlers.ofString());

        assertTrue(result.isSuccess());
        assertSame(success, result.getContent());
        assertEquals(List.of(endpoints.get(0), endpoints.get(1), endpoints.get(0), endpoints.get(1),
            endpoints.get(0), endpoints.get(1)), selected);
        assertEquals(endpoints, proxies.getProxiesSnapshot());
        Mockito.verify(transport, Mockito.times(6))
            .send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void discardsFailedEndpointAfterOtherSelectionsAdvanceRotation() throws Exception {
        var settings = settings(3);
        var proxies = settings.getProxyClientSettings();
        var endpoints = proxies.getProxiesSnapshot();
        var transport = Mockito.mock(java.net.http.HttpClient.class);
        Mockito.when(transport.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
            .thenAnswer(invocation -> {
                // Simulate other requests selecting proxies while the first request is in flight.
                assertEquals(endpoints.get(1), proxies.next());
                assertEquals(endpoints.get(2), proxies.next());
                throw new ConnectException("The first selected proxy could not connect");
            });

        assertThrows(TikTokProxyRequestException.class, () ->
            client(settings, transport).toHttpResponse(HttpResponse.BodyHandlers.ofString()));

        assertEquals(List.of(endpoints.get(1), endpoints.get(2)), proxies.getProxiesSnapshot());
        assertEquals(endpoints.get(1), proxies.next());
        Mockito.verify(transport)
            .send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void directFallbackFailureReturnsWithoutAnotherProxyAttempt() throws Exception {
        var settings = settings(2);
        var proxies = settings.getProxyClientSettings();
        proxies.setFallback(true);
        var endpoints = proxies.getProxiesSnapshot();
        var selected = new ArrayList<ProxyData>();
        proxies.setOnProxyUpdated(selected::add);
        var transport = Mockito.mock(java.net.http.HttpClient.class);
        var directFailure = response(429);
        var unexpectedSuccess = response(200);
        Mockito.when(transport.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
            .thenThrow(new IOException("Proxy CONNECT failed with status 503"))
            .thenReturn(directFailure, unexpectedSuccess);

        var result = client(settings, transport).toHttpResponse(HttpResponse.BodyHandlers.ofString());

        assertTrue(result.isFailure());
        assertSame(directFailure, result.getContent());
        assertEquals(List.of(endpoints.get(0)), selected);
        assertEquals(endpoints, proxies.getProxiesSnapshot());
        Mockito.verify(transport, Mockito.times(2))
            .send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any());
    }

    private HttpClientSettings settings(int proxyCount) {
        var settings = new HttpClientSettings();
        var proxies = settings.getProxyClientSettings();
        proxies.setEnabled(true);
        proxies.setType(Proxy.Type.HTTP);
        for (int offset = 0; offset < proxyCount; offset++)
            proxies.addProxy(InetSocketAddress.createUnresolved("127.0.0.1", 8000 + offset));
        return settings;
    }

    @SuppressWarnings("unchecked")
    private HttpResponse<String> response(int status) {
        var response = (HttpResponse<String>) Mockito.mock(HttpResponse.class);
        Mockito.when(response.statusCode()).thenReturn(status);
        Mockito.when(response.headers()).thenReturn(HttpHeaders.of(Map.of(), (name, value) -> true));
        return response;
    }

    private HttpProxyClient client(HttpClientSettings settings, java.net.http.HttpClient transport) {
        var ownerSettings = new LiveClientSettings();
        ownerSettings.setHttpSettings(settings);
        var factory = new HttpClientFactory(ownerSettings) {
            @Override
            java.net.http.HttpClient getHttpClient() {
                return transport;
            }
        };
        return new HttpProxyClient(settings, "http://request.example/test", null, factory);
    }
}
