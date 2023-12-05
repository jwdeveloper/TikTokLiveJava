package io.github.jwdeveloper.tiktok.http;

import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.Constants;
import io.github.jwdeveloper.tiktok.data.dto.TikTokUserInfo;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.regex.Pattern;

public class TikTokDataChecker {

    public CompletableFuture<Boolean> isOnlineAsync(String hostName) {
        return CompletableFuture.supplyAsync(() -> isOnline(hostName));
    }

    public CompletableFuture<Boolean> isHostNameValidAsync(String hostName) {
        return CompletableFuture.supplyAsync(() -> isOnline(hostName));
    }

    public boolean isOnline(String hostName) {
        var data = getApiService().fetchUserInfoFromTikTokApi(hostName);
        return data.getUserStatus() == TikTokUserInfo.UserStatus.Live ||
                data.getUserStatus() == TikTokUserInfo.UserStatus.LivePaused;
    }

    public boolean isHostNameValid(String hostName) {
        var data = getApiService().fetchUserInfoFromTikTokApi(hostName);
        return data.getUserStatus() != TikTokUserInfo.UserStatus.NotFound;
    }

    public TikTokApiService getApiService() {
        var jar = new TikTokCookieJar();
        var factory = new TikTokHttpRequestFactory(jar);
        var client = new TikTokHttpClient(jar, factory);
        var settings = new ClientSettings();
        settings.setClientParameters(Constants.DefaultClientParams());
        var apiService = new TikTokApiService(client, Logger.getGlobal(), settings);
        return apiService;
    }

}
