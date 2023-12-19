/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.http;

import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.Constants;
import io.github.jwdeveloper.tiktok.data.dto.TikTokUserInfo;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventObserver;

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
        var factory = new TikTokHttpRequestFactory(jar,new TikTokEventObserver());
        var client = new TikTokHttpClient(jar, factory);
        var settings = new ClientSettings();
        settings.setClientParameters(Constants.DefaultClientParams());
        var apiService = new TikTokApiService(client, Logger.getGlobal(), settings);
        return apiService;
    }

}
