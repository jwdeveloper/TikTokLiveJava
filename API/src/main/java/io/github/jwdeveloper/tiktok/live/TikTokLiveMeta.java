package io.github.jwdeveloper.tiktok.live;

import lombok.Data;

@Data
public class TikTokLiveMeta implements LiveMeta
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
