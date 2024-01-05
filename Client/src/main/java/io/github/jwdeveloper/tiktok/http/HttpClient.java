package io.github.jwdeveloper.tiktok.http;

import io.github.jwdeveloper.tiktok.data.settings.HttpClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import lombok.AllArgsConstructor;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@AllArgsConstructor
public class HttpClient {
    private final HttpClientSettings httpClientSettings;
    private final String url;


    public <T> Optional<HttpResponse<T>> toResponse(HttpResponse.BodyHandler<T> bodyHandler) {
        var client = prepareClient();
        var request = prepareGetRequest();
        try
        {
            var response = client.send(request, bodyHandler);
            if(response.statusCode() != 200)
            {
                return Optional.empty();
            }



            return Optional.of(response);
        } catch (Exception e) {
            throw new TikTokLiveRequestException(e);
        }
    }


    public Optional<String> toJsonResponse() {
        var optional = toResponse(HttpResponse.BodyHandlers.ofString());
        if (optional.isEmpty()) {
            return Optional.empty();
        }

        var response = optional.get();


        var body = response.body();
        return Optional.of(body);
    }

    public Optional<byte[]> toBinaryResponse() {
        var optional = toResponse(HttpResponse.BodyHandlers.ofByteArray());
        if (optional.isEmpty()) {
            return Optional.empty();
        }
        var body = optional.get().body();
        return Optional.of(body);
    }


    public URI toUrl() {
        var stringUrl = prepareUrlWithParameters(url, httpClientSettings.getParams());
        return URI.create(stringUrl);
    }

    private HttpRequest prepareGetRequest() {
        var requestBuilder = HttpRequest.newBuilder().GET();
        requestBuilder.uri(toUrl());
        requestBuilder.timeout(httpClientSettings.getTimeout());
        httpClientSettings.getHeaders().forEach(requestBuilder::setHeader);

        httpClientSettings.getOnRequestCreating().accept(requestBuilder);
        return requestBuilder.build();
    }

    private java.net.http.HttpClient prepareClient() {
        var builder = java.net.http.HttpClient.newBuilder()
                .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
                .cookieHandler(new CookieManager())
                .connectTimeout(httpClientSettings.getTimeout());


        httpClientSettings.getOnClientCreating().accept(builder);
        return builder.build();
    }

    private String prepareUrlWithParameters(String url, Map<String, Object> parameters) {
        if (parameters.isEmpty()) {
            return url;
        }

        return url + "?" + parameters.entrySet().stream().map(entry ->
        {
            var encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            var encodedValue = URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8);
            return encodedKey + "=" + encodedValue;
        }).collect(Collectors.joining("&"));
    }
}
