package io.github.jwdeveloper.tiktok.live;

import lombok.Data;

@Data
public class TikTokRoomInfo implements LiveRoomInfo
{
    private int viewersCount;

    private String roomId;

    private String userName;
    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public boolean hasConnectionState(ConnectionState state)
    {
        return connectionState == state;
    }
}
