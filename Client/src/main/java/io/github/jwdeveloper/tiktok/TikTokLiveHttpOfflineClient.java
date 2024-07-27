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
package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.data.requests.GiftsData;
import io.github.jwdeveloper.tiktok.data.requests.LiveConnectionData;
import io.github.jwdeveloper.tiktok.data.requests.LiveData;
import io.github.jwdeveloper.tiktok.data.requests.LiveUserData;
import io.github.jwdeveloper.tiktok.http.LiveHttpClient;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;

import java.net.URI;
import java.util.List;

public class TikTokLiveHttpOfflineClient implements LiveHttpClient {
    @Override
    public GiftsData.Response fetchGiftsData() {
        return new GiftsData.Response("", List.of());
    }

    @Override
    public GiftsData.Response fetchRoomGiftsData(String room_id) {
        return new GiftsData.Response("", List.of());
    }

    @Override
    public LiveUserData.Response fetchLiveUserData(LiveUserData.Request request) {
        return new LiveUserData.Response("", LiveUserData.UserStatus.Live, "offline_room_id", 0, null);
    }

    @Override
    public LiveData.Response fetchLiveData(LiveData.Request request) {
        return new LiveData.Response("",
                LiveData.LiveStatus.HostOnline,
                "offline live",
                0,
                0,
                0,
                false,
                new User(0L, "offline user", new Picture("")),
                LiveData.LiveType.SOLO);
    }

    @Override
    public LiveConnectionData.Response fetchLiveConnectionData(LiveConnectionData.Request request) {
        return new LiveConnectionData.Response("",
                URI.create("https://example.live"),
                WebcastResponse.newBuilder().build());
    }
}