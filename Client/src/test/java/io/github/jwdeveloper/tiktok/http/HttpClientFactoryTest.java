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

import io.github.jwdeveloper.tiktok.client.TikTokLiveHttpClient;
import io.github.jwdeveloper.tiktok.api.data.dto.ProxyData;
import io.github.jwdeveloper.tiktok.api.data.settings.HttpClientSettings;
import io.github.jwdeveloper.tiktok.api.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.client.http.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class HttpClientFactoryTest {
    private static final String URL = "http://request.example/test";

    @Test
    void requestSettingsAreClonedWhileOwnerTransportIsReused() {
        var settings = LiveClientSettings.createDefault();
        var factory = new HttpClientFactory(settings);
        var first = factory.client(URL).withCookie("session", "first").build();
        var second = factory.client(URL).withCookie("session", "second").build();

        assertNotSame(first.httpClientSettings, second.httpClientSettings);
        assertNotSame(settings.getHttpSettings(), first.httpClientSettings);
        assertEquals("session=first", first.prepareRequest().headers().firstValue("Cookie").orElseThrow());
        assertEquals("session=second", second.prepareRequest().headers().firstValue("Cookie").orElseThrow());
        assertTrue(settings.getHttpSettings().getCookies().isEmpty());
        assertSame(factory, first.httpClientFactory);
        assertSame(factory.getHttpClient(), first.httpClientFactory.getHttpClient());
        assertSame(first.httpClientFactory.getHttpClient(), second.httpClientFactory.getHttpClient());
    }

    @Test
    void liveOwnersHaveIndependentTransportsAndCookieStoresEvenWithSameCustomizer() {
        var calls = new AtomicInteger();
        Consumer<java.net.http.HttpClient.Builder> customizer = builder -> calls.incrementAndGet();
        var firstSettings = LiveClientSettings.createDefault();
        var secondSettings = LiveClientSettings.createDefault();
        firstSettings.getHttpSettings().onClientCreating(customizer);
        secondSettings.getHttpSettings().onClientCreating(customizer);
        var first = new HttpClientFactory(firstSettings).getHttpClient();
        var second = new HttpClientFactory(secondSettings).getHttpClient();

        assertNotSame(first, second);
        assertNotSame(first.cookieHandler().orElseThrow(), second.cookieHandler().orElseThrow());
        assertEquals(2, calls.get());
    }

    @Test
    void repeatedBuildsFromStandaloneBuilderReuseItsTransport() {
        var builder = new HttpClientBuilder(URL, new HttpClientSettings());
        var first = builder.build();
        var second = builder.withUrl(URL + "/second").build();

        assertSame(first.httpClientFactory.getHttpClient(), second.httpClientFactory.getHttpClient());
    }

    @Test
    void consumerConfigurationIsReadLazilyAndCustomizerRunsOnce() throws ReflectiveOperationException {
        var calls = new AtomicInteger();
        var requests = new TikTokLiveHttpClient(settings -> {
            settings.getHttpSettings().setTimeout(Duration.ofSeconds(17));
            settings.getHttpSettings().onClientCreating(builder -> {
                calls.incrementAndGet();
                builder.followRedirects(java.net.http.HttpClient.Redirect.NEVER);
            });
        });
        assertEquals(0, calls.get(), "Constructing the requests client must not build its transport");
        var field = TikTokLiveHttpClient.class.getDeclaredField("httpFactory");
        field.setAccessible(true);
        var factory = (HttpClientFactory) field.get(requests);
        var first = factory.client(URL).build().httpClientFactory.getHttpClient();
        var second = factory.client(URL + "/second").build().httpClientFactory.getHttpClient();

        assertSame(first, second);
        assertSame(first, factory.getHttpClient());
        assertEquals(Duration.ofSeconds(17), first.connectTimeout().orElseThrow());
        assertEquals(java.net.http.HttpClient.Redirect.NEVER, first.followRedirects());
        assertEquals(1, calls.get());
    }

    @Test
    void selectedProxyIsBoundForSendAndClearedAfterFailure() throws Exception {
        var settings = LiveClientSettings.createDefault();
        settings.getHttpSettings().getProxyClientSettings().setEnabled(true);
        settings.getHttpSettings().getProxyClientSettings().setType(Proxy.Type.HTTP);
        var factory = new HttpClientFactory(settings);
        var selector = factory.getHttpClient().proxy().orElseThrow();
        var endpoint = new ProxyData("127.0.0.1", 8080);
        var expectedProxy = new Proxy(Proxy.Type.HTTP, endpoint.toSocketAddress());
        var request = HttpRequest.newBuilder(URI.create(URL)).build();
        var transport = Mockito.mock(java.net.http.HttpClient.class);
        Mockito.when(transport.send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any()))
            .thenAnswer(invocation -> {
                assertEquals(List.of(expectedProxy), selector.select(request.uri()));
                throw new IOException("Simulated proxy failure");
            });
        setTransport(factory, transport);

        assertThrows(IOException.class, () ->
            factory.send(request, HttpResponse.BodyHandlers.ofString(), endpoint));
        assertThrows(IllegalStateException.class, () -> selector.select(request.uri()));
        Mockito.verify(transport)
            .send(Mockito.any(HttpRequest.class), Mockito.<HttpResponse.BodyHandler<String>>any());
    }

    @Test
    void disabledProxiesWithPopulatedListUseNativeClientWithoutRoutingState() throws Exception {
        var settings = LiveClientSettings.createDefault();
        var proxies = settings.getHttpSettings().getProxyClientSettings();
        proxies.setType(Proxy.Type.HTTP);
        proxies.addProxy(InetSocketAddress.createUnresolved("127.0.0.1", 8080));
        proxies.setEnabled(false);

        assertUnroutedNativeClientAndMockSend(settings);
    }

    @Test
    void socksSettingsLeaveNativeHttpClientWithoutRoutingState() throws Exception {
        var settings = LiveClientSettings.createDefault();
        var proxies = settings.getHttpSettings().getProxyClientSettings();
        proxies.setType(Proxy.Type.SOCKS);
        proxies.addProxy(InetSocketAddress.createUnresolved("127.0.0.1", 8080));
        proxies.setEnabled(true);

        assertUnroutedNativeClientAndMockSend(settings);
    }

    @SuppressWarnings("unchecked")
    private void assertUnroutedNativeClientAndMockSend(LiveClientSettings settings) throws Exception {
        var factory = new HttpClientFactory(settings);
        assertNull(selectorState(factory));
        assertTrue(factory.getHttpClient().proxy().isEmpty());
        assertNull(selectorState(factory));
        var transport = Mockito.mock(java.net.http.HttpClient.class);
        var request = HttpRequest.newBuilder(URI.create(URL)).build();
        var handler = HttpResponse.BodyHandlers.ofString();
        var response = (HttpResponse<String>) Mockito.mock(HttpResponse.class);
        Mockito.when(transport.send(request, handler)).thenReturn(response);
        setTransport(factory, transport);

        assertSame(response, factory.send(request, handler));
        assertNull(selectorState(factory), "An ordinary HTTP send must not allocate proxy routing state");
        Mockito.verify(transport).send(request, handler);
    }

    private Object selectorState(HttpClientFactory factory) throws ReflectiveOperationException {
        var field = HttpClientFactory.class.getDeclaredField("proxySelector");
        field.setAccessible(true);
        return field.get(factory);
    }

    private void setTransport(HttpClientFactory factory, java.net.http.HttpClient transport)
        throws ReflectiveOperationException {
        var field = HttpClientFactory.class.getDeclaredField("httpClient");
        field.setAccessible(true);
        field.set(factory, transport);
    }
}