package io.github.jwdeveloper.tiktok.data.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TikTokUserInfo
{
    UserStatus userStatus;

    String roomId;

    public enum UserStatus
    {
        NotFound,
        Offline,
        LivePaused,
        Live
    }
}
