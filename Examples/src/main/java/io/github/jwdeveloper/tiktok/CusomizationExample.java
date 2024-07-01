package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.data.settings.LiveClientSettings;
import io.github.jwdeveloper.tiktok.http.HttpClientBuilder;
import io.github.jwdeveloper.tiktok.http.HttpClientFactory;
import io.github.jwdeveloper.tiktok.live.GiftsManager;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.live.LiveEventsHandler;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * When the default implementation does not meet your needs,
 * you can override it using `customize` method
 */
public class CusomizationExample {
    public static void main(String[] args) {

        var customEventHandler = new CustomEventsHandler();
        var client = TikTokLive.newClient("john")
                .configure(liveClientSettings ->
                {
                    liveClientSettings.setFetchGifts(false);
                    liveClientSettings.setOffline(true);
                })
                .onError((liveClient, event) ->
                {
                    event.getException().printStackTrace();
                })
                .customize(container ->
                {
                    //overriding default implementation of LiveEventsHandler, with own one
                    container.registerSingleton(LiveEventsHandler.class, customEventHandler);
                }).build();

        client.connect();
        client.publishEvent(TikTokGiftEvent.of("rose", 1, 12));
        client.publishEvent(TikTokGiftEvent.of("stone", 2, 12));
    }


    public static class CustomEventsHandler extends TikTokLiveEventHandler {

        @Override
        public void publish(LiveClient tikTokLiveClient, TikTokEvent tikTokEvent) {
            System.out.println("Hello from custom events handler: " + tikTokEvent.getClass().getSimpleName());
        }
    }
}
