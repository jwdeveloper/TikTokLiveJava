package io.github.jwdeveloper.tiktok.live;

public interface LiveClient {

    void connect();

    void disconnect();

    void sendHeartBeat();

    GiftManager getGiftManager();
    LiveRoomInfo getRoomInfo();
}
