package io.github.jwdeveloper.tiktok.data.requests;

import io.github.jwdeveloper.tiktok.data.models.users.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

public class LiveData {
    @Getter
    @AllArgsConstructor
    public static class Request {
        private String roomId;
    }

    @Data
    public static class Response {
        private String json;
        private LiveStatus liveStatus;
        private String title;
        private int likes;
        private int viewers;
        private int totalViewers;
        private boolean ageRestricted;
        private User host;
    }

    public enum LiveStatus {
        HostNotFound,
        HostOnline,
        HostOffline,
    }
}
