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

        return new HttpClient(httpClientSettings, url);
    }


}
