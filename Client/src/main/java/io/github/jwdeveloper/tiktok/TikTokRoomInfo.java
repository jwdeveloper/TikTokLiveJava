package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.live.ConnectionState;
import io.github.jwdeveloper.tiktok.live.LiveRoomInfo;
import lombok.Data;

@Data
public class TikTokRoomInfo implements LiveRoomInfo
{
    private int viewersCount;

    private String roomId;

    private boolean ageRestricted;

    private String userName;

    private ConnectionState connectionState = ConnectionState.DISCONNECTED;

    public boolean hasConnectionState(ConnectionState state)
    {
        return connectionState == state;
    }
}
