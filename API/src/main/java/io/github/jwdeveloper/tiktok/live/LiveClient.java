package io.github.jwdeveloper.tiktok.live;

public interface LiveClient {

    void connect();

    void disconnect();

    void sendHeartbeat();

    GiftManager getGiftManager();
    LiveRoomInfo getRoomInfo();
}
