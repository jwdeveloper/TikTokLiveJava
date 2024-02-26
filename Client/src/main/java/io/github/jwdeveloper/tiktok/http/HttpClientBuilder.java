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

import io.github.jwdeveloper.tiktok.data.settings.HttpClientSettings;

import java.util.Map;
import java.util.function.Consumer;

public class HttpClientBuilder {

    private final HttpClientSettings httpClientSettings;
    private String url;

    public HttpClientBuilder(String url, HttpClientSettings httpClientSettings) {
        this.httpClientSettings = httpClientSettings;
        this.url = url;
    }

    public HttpClientBuilder(String url) {
        httpClientSettings = new HttpClientSettings();
        this.url = url;
    }

    public HttpClientBuilder withUrl(String url) {
        this.url = url;
        return this;
    }

    public HttpClientBuilder withHttpClientSettings(Consumer<HttpClientSettings> consumer) {
        consumer.accept(httpClientSettings);
        return this;
    }

    public HttpClientBuilder withCookie(String name, String value) {
        httpClientSettings.getCookies().put(name, value);
        return this;
    }

    public HttpClientBuilder withHeader(String name, String value) {
        httpClientSettings.getHeaders().put(name, value);
        return this;
    }

    public HttpClientBuilder withParam(String name, String value) {
        httpClientSettings.getParams().put(name, value);
        return this;
    }

    public HttpClientBuilder withParams(Map<String, String> parameters) {
        httpClientSettings.getParams().putAll(parameters);
        return this;
    }

    public HttpClientBuilder withHeaders(Map<String, String> headers) {
        httpClientSettings.getHeaders().putAll(headers);
        return this;
    }

    public HttpClient build() {
        var proxyClientSettings = httpClientSettings.getProxyClientSettings();
        if (proxyClientSettings.isEnabled() && proxyClientSettings.hasNext())
            return new HttpProxyClient(httpClientSettings, url);
        return new HttpClient(httpClientSettings, url);
    }
}