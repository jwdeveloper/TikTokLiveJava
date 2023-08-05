package io.github.jwdeveloper.tiktok.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TikTokCookieJar {
    /// <summary>
    /// Cookies in Jar
    /// </summary>
    private final Map<String, String> cookies;

    /// <summary>
    /// Create a TikTok cookie jar instance.
    /// </summary>
    public TikTokCookieJar() {
        cookies = new HashMap<>();
    }


    public String get(String key) {
        return cookies.get(key);
    }

    public void set(String key, String value) {
        cookies.put(key, value);
    }

    /// <summary>
    /// Enumerates Cookies
    /// </summary>
    public Set<Map.Entry<String, String>> GetEnumerator() {
        return cookies.entrySet();
    }

  /*  /// <summary>
    /// Enumerates Cookies
    /// </summary>
    public IEnumerator<string> GetEnumerator()
    {
        foreach (var cookie in cookies)
        yield return $"{cookie.Key}={cookie.Value};";
    }*/
}
