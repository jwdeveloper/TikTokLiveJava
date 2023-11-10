package io.github.jwdeveloper.tiktok.http;

import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;

import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;

public class TikTokDataChecker
{

    public CompletableFuture<Boolean> isOnlineAsync(String hostName) {
        return CompletableFuture.supplyAsync(() -> isOnline(hostName));
    }

    public CompletableFuture<Boolean> isHostNameValidAsync(String hostName) {
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

    public boolean isHostNameValid(String hostName) {
        var factory = new TikTokHttpRequestFactory(new TikTokCookieJar());
        var url = getProfileUrl(hostName);
        try {
            var response = factory.get(url);
            var titleContent = extractTitleContent(response);
            return isTitleHostNameValid(titleContent, hostName);
        } catch (Exception e)
        {
            throw new TikTokLiveRequestException("Unable to make check host name valid request",e);
        }
    }

    private boolean isTitleLiveOnline(String title) {
        return title.contains("is LIVE");
    }

    private boolean isTitleHostNameValid(String title, String hostName)
    {
        return title.contains(hostName);
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

    private String getProfileUrl(String hostName) {
        var sb = new StringBuilder();
        sb.append("https://www.tiktok.com/@");
        sb.append(hostName);
        return sb.toString();
    }
}
