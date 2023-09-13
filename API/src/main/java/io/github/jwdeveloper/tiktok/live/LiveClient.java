package io.github.jwdeveloper.tiktok.live;

import io.github.jwdeveloper.tiktok.listener.ListenersManager;
import io.github.jwdeveloper.tiktok.listener.TikTokEventListener;

public interface LiveClient {

    /**
     * Connects to the live stream.
     */
    void connect();

    /**
     * Disconnects the connection.
     */
    void disconnect();


    /**
     * Get information about gifts
     */
    GiftManager getGiftManager();

    /**
     * Gets the current room info from TikTok API including streamer info, room status and statistics.
     */
    LiveRoomInfo getRoomInfo();

    /**
     * Manage TikTokEventListener
     * @see TikTokEventListener
     */
    ListenersManager getListenersManager();
}
