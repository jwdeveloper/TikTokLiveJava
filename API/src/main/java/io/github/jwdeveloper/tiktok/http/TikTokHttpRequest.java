package io.github.jwdeveloper.tiktok.http;

import java.net.http.HttpRequest;
import java.util.Map;

public interface TikTokHttpRequest {
    TikTokHttpRequest SetQueries(Map<String, Object> queries);

    TikTokHttpRequest setHeader(String key, String value);
    String Get(String url);

    String Post(String url, HttpRequest.BodyPublisher data);
}
