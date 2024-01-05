package io.github.jwdeveloper.tiktok.http;

import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;

public class HttpClientFactory {
    private final LiveClientSettings liveClientSettings;

    public HttpClientFactory(LiveClientSettings liveClientSettings) {
        this.liveClientSettings = liveClientSettings;
    }

    public HttpClientBuilder client(String url) {
        return new HttpClientBuilder(url, liveClientSettings.getHttpSettings().clone());
    }

    //Does not contains default httpClientSettings, Params, headers, etd
    public HttpClientBuilder clientEmpty(String url) {
        return new HttpClientBuilder(url);
    }
}
