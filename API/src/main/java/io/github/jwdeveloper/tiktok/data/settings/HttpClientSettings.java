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
package io.github.jwdeveloper.tiktok.data.settings;

import lombok.Getter;
import lombok.Setter;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;


@Getter
public class HttpClientSettings {

    private final Map<String, Object> params;

    private final Map<String, String> headers;

    private final Map<String, String> cookies;

    @Setter
    private ProxyClientSettings proxyClientSettings;

    private Consumer<HttpClient.Builder> onClientCreating;

    private Consumer<HttpRequest.Builder> onRequestCreating;

    @Setter
    private Duration timeout;

    public HttpClientSettings() {
        this.params = new TreeMap<>();
        this.headers = new HashMap<>();
        this.cookies = new HashMap<>();
        this.timeout = Duration.ofSeconds(2);
        this.proxyClientSettings = new ProxyClientSettings();
        this.onClientCreating = (x) -> {};
        this.onRequestCreating = (x) -> {};
    }

    /**
     * @param consumer Use to configure proxy settings for http client
     */
    public void configureProxy(Consumer<ProxyClientSettings> consumer) {
        proxyClientSettings.setEnabled(true);
        consumer.accept(proxyClientSettings);
    }

    /**
     * @param onRequestCreating Every time new Http request in created this method will be triggered
     *                          use to modify request
     */
    public void onRequestCreating(Consumer<HttpRequest.Builder> onRequestCreating) {
        this.onRequestCreating = onRequestCreating;
    }

    /**
     * @param onClientCreating Every time new instance of Http client request in created this method will be triggered
     *                         use to modify http client
     */
    public void onClientCreating(Consumer<HttpClient.Builder> onClientCreating) {
        this.onClientCreating = onClientCreating;
    }

    @Override
    public HttpClientSettings clone() {

        var newSettings = new HttpClientSettings();
        newSettings.setTimeout(this.getTimeout());
        newSettings.onRequestCreating(this.onRequestCreating);
        newSettings.onClientCreating(this.onClientCreating);
        newSettings.getHeaders().putAll(new TreeMap<>(this.headers));
        newSettings.getCookies().putAll(new TreeMap<>(this.cookies));
        newSettings.getParams().putAll(new TreeMap<>(this.params));
        newSettings.setProxyClientSettings(this.proxyClientSettings);

        return newSettings;
    }
}