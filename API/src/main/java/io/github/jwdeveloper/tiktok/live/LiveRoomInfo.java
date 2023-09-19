package io.github.jwdeveloper.tiktok.live;

public interface LiveRoomInfo
{
     int getViewersCount();
     boolean isAgeRestricted();
     String getRoomId();
     String getUserName();
     ConnectionState getConnectionState();
}
