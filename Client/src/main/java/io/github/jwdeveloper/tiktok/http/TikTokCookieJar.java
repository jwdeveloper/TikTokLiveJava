package io.github.jwdeveloper.tiktok.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TikTokCookieJar {
    private final Map<String, String> cookies;
    public TikTokCookieJar() {
        cookies = new HashMap<>();
    }

    public String get(String key) {
        return cookies.get(key);
    }

    public void set(String key, String value) {
        cookies.put(key, value);
    }

    public Set<Map.Entry<String, String>> GetEnumerator() {
        return cookies.entrySet();
    }

    public String parseCookies()
    {
        var sb = new StringBuilder();
        for(var entry : cookies.entrySet())
        {
            sb.append(entry.getKey()).append("=").append(entry.getValue()).append(";");
        }
        return sb.toString();
    }
}
