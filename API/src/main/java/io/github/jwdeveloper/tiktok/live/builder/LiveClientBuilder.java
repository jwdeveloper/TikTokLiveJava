package io.github.jwdeveloper.tiktok.live.builder;

import io.github.jwdeveloper.tiktok.ClientSettings;
import io.github.jwdeveloper.tiktok.listener.TikTokEventListener;
import io.github.jwdeveloper.tiktok.live.LiveClient;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface LiveClientBuilder extends EventBuilder<LiveClientBuilder>
{

    LiveClientBuilder configure(Consumer<ClientSettings> consumer);

    LiveClientBuilder addListener(TikTokEventListener listener);

    LiveClient build();
    LiveClient buildAndConnect();

    CompletableFuture<LiveClient> buildAndConnectAsync();
}
