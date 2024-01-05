package io.github.jwdeveloper.tiktok.data.requests;

import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.net.URI;
import java.time.Duration;

public class LiveConnectionData {
    @Getter
    @AllArgsConstructor
    public static class Request {
        private String roomId;
    }

    @Getter
    @AllArgsConstructor
    public static class Response {
        private String websocketCookies;
        private URI websocketUrl;
        private WebcastResponse webcastResponse;
    }
}
