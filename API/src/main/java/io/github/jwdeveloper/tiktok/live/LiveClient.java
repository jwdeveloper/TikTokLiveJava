package io.github.jwdeveloper.tiktok.live;

import io.github.jwdeveloper.tiktok.listener.ListenersManager;

public interface LiveClient {

    // Connects to the live stream.
    void connect();

    // Disconnects the connection.
    void disconnect();

    // Gets the meta information about all gifts.
    GiftManager getGiftManager();

    // Gets the current room info from TikTok API including streamer info, room status and statistics.
    LiveRoomInfo getRoomInfo();
    ListenersManager getListenersManager();
}
