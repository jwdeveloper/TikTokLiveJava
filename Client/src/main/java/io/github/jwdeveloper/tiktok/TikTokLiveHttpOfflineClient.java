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
        return new LiveUserData.Response("", LiveUserData.UserStatus.Live, "offline_room_id", 0);
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