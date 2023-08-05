package io.github.jwdeveloper.tiktok.live;

public interface LiveClient {
    void run();

    void stop();

    LiveMeta getMeta();
}
