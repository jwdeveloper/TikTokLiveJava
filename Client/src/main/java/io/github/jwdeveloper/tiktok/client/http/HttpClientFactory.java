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
package io.github.jwdeveloper.tiktok.client.http;

import io.github.jwdeveloper.tiktok.api.data.dto.ProxyData;
import io.github.jwdeveloper.tiktok.api.data.settings.*;
import lombok.Getter;

import java.io.IOException;
import java.net.*;
import java.net.http.*;

public class HttpClientFactory {
    @Getter
    private final LiveClientSettings liveClientSettings;
    private RotatingProxySelector proxySelector;
    private java.net.http.HttpClient httpClient;

    public HttpClientFactory(LiveClientSettings liveClientSettings) {
        this.liveClientSettings = liveClientSettings;
    }

    public HttpClientBuilder client(String url) {
        return new HttpClientBuilder(url, liveClientSettings.getHttpSettings().clone(), this);
    }

    static HttpClientFactory forSettings(HttpClientSettings settings) {
        var liveSettings = new LiveClientSettings();
        liveSettings.setHttpSettings(settings);
        return new HttpClientFactory(liveSettings);
    }

    /** Creates the owning LiveClient's transport once, after its settings have been configured. */
    synchronized java.net.http.HttpClient getHttpClient() {
        if (httpClient == null) {
            var settings = liveClientSettings.getHttpSettings();
            var builder = java.net.http.HttpClient.newBuilder()
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .cookieHandler(new CookieManager())
                .connectTimeout(settings.getTimeout());
            var proxySettings = settings.getProxyClientSettings();
            if (proxySettings.isEnabled() && proxySettings.getType() != Proxy.Type.SOCKS) {
                proxySelector = new RotatingProxySelector();
                builder.proxy(proxySelector);
            }
            settings.getOnClientCreating().accept(builder);
            httpClient = builder.build();
        }
        return httpClient;
    }

    public <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> handler) throws IOException, InterruptedException {
        return send(request, handler, null);
    }

    <T> HttpResponse<T> send(HttpRequest request, HttpResponse.BodyHandler<T> handler, ProxyData endpoint) throws IOException, InterruptedException {
        var client = getHttpClient();
        var selector = proxySelector;
        if (selector == null)
            return client.send(request, handler);
        var proxy = selector.bind(endpoint);
        try {
            return client.send(request, handler);
        } finally {
            selector.clear(proxy);
        }
    }
}