package io.github.jwdeveloper.tiktok.http;

import java.util.Map;

public interface TikTokHttpRequest {
    TikTokHttpRequest setQueries(Map<String, Object> queries);

    TikTokHttpRequest setHeader(String key, String value);
    String get(String url);

    String post(String url);
}
