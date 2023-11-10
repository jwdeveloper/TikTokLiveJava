package io.github.jwdeveloper.tiktok.http;

import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.live.LiveClient;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class TikTokLiveOnlineChecker
{

    public CompletableFuture<Boolean> isOnlineAsync(String hostName) {
        return CompletableFuture.supplyAsync(() -> isOnline(hostName));
    }

    public boolean isOnline(String hostName) {
        var factory = new TikTokHttpRequestFactory(new TikTokCookieJar());
        var url = getLiveUrl(hostName);
        try {
            var response = factory.get(url);
            var titleContent = extractTitleContent(response);
            return isTitleLiveOnline(titleContent);
        } catch (Exception e)
        {
            throw new TikTokLiveRequestException("Unable to make check live online request",e);
        }
    }

    private boolean isTitleLiveOnline(String title) {
        return title.contains("is LIVE");
    }

    private String extractTitleContent(String html) {
        var regex = "<title\\b[^>]*>(.*?)<\\/title>";
        var pattern = Pattern.compile(regex);
        var matcher = pattern.matcher(html);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return "";
        }
    }

    private String getLiveUrl(String hostName) {
        var sb = new StringBuilder();
        sb.append("https://www.tiktok.com/@");
        sb.append(hostName);
        sb.append("/live");
        return sb.toString();
    }
}
