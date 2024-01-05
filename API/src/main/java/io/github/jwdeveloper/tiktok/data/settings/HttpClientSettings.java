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


public class HttpClientSettings {

    @Getter
    final Map<String, Object> params;

    @Getter
    final Map<String, String> headers;

    @Getter
    final Map<String, String> cookies;

    @Getter
    ProxyClientSettings proxyClientSettings;

    @Getter
    Consumer<HttpClient.Builder> onClientCreating;

    @Getter
    Consumer<HttpRequest.Builder> onRequestCreating;

    @Setter
    @Getter
    Duration timeout;

    public HttpClientSettings() {
        this.params = new TreeMap<>();
        this.headers = new HashMap<>();
        this.cookies = new HashMap<>();
        this.timeout = Duration.ofSeconds(2);
        this.proxyClientSettings = new ProxyClientSettings();
        this.onClientCreating = (x) -> {
        };
        this.onRequestCreating = (x) -> {
        };
    }

    /**
     * @param consumer Use to configure proxy settings for http client
     */
    public void configureProxy(Consumer<ProxyClientSettings> consumer) {
        proxyClientSettings.setUseProxy(true);
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
        newSettings.proxyClientSettings = this.proxyClientSettings.clone();

        return newSettings;
    }
}
