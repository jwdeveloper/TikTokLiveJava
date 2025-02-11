/*
 * Copyright (c) 2023-2024 jwdeveloper jacekwoln@gmail.com
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
package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.http.LiveHttpClient;
import io.github.jwdeveloper.tiktok.live.builder.LiveClientBuilder;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class TikTokLive
{

    /**
     * Example: {@code https://www.tiktok.com/@dostawcavideo} - hostName would be 'dostawcavideo'
     *
     * @param hostName profile name of TikTok user could be found in profile link
     * @return LiveClientBuilder
     */
    public static LiveClientBuilder newClient(String hostName) {
        return new TikTokLiveClientBuilder(hostName);
    }

    /**
     * Example: {@code https://www.tiktok.com/@dostawcavideo} - hostName would be 'dostawcavideo'
     *
     * @param hostName profile name of TikTok user could be found in profile link
     * @return true if live is Online, false if is offline
     */
    public static boolean isLiveOnline(String hostName) {
        return requests().fetchLiveUserData(hostName).isLiveOnline();
    }

    /**
     * Example: {@code https://www.tiktok.com/@dostawcavideo} - hostName would be 'dostawcavideo'
     *
     * @param hostName profile name of TikTok user could be found in profile link
     * @return {@link CompletableFuture} of true if live is Online, false if is offline
     */
    public static CompletableFuture<Boolean> isLiveOnlineAsync(String hostName) {
        return CompletableFuture.supplyAsync(() -> isLiveOnline(hostName));
    }

    /**
     * Example: {@code https://www.tiktok.com/@dostawcavideo} - hostName would be 'dostawcavideo'
     *
     * @param hostName profile name of TikTok user could be found in profile link
     * @return true is hostName name is valid and exists, false if not
     */
    public static boolean isHostNameValid(String hostName) {
        return requests().fetchLiveUserData(hostName).isHostNameValid();
    }

    /**
     * Example: {@code https://www.tiktok.com/@dostawcavideo} - hostName would be 'dostawcavideo'
     *
     * @param hostName profile name of TikTok user could be found in profile link
     * @return true is hostName name is valid and exists, false if not
     */
    public static CompletableFuture<Boolean> isHostNameValidAsync(String hostName) {
        return CompletableFuture.supplyAsync(() -> isHostNameValid(hostName));
    }

    /**
     * Use to get some data from TikTok about users are lives
     *
     * @return LiveHttpClient
     */
    public static LiveHttpClient requests(Consumer<LiveClientSettings> consumer) {
        return new TikTokLiveHttpClient(consumer);
    }

    /**
     * Use to get some data from TikTok about users are lives
     *
     * @return LiveHttpClient
     */
    public static LiveHttpClient requests() {
        return requests(liveClientSettings -> {});
    }
}