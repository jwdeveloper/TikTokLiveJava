package io.github.jwdeveloper.tiktok.data.requests;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

public class LiveUserData {

    @Getter
    @AllArgsConstructor
    public static class Request {
        private String userName;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {

        private String json;

        private UserStatus userStatus;

        private String roomId;


        private long startedAtTimeStamp;
    }

    public enum UserStatus {
        NotFound,
        Offline,
        LivePaused,
        Live,
    }
}


