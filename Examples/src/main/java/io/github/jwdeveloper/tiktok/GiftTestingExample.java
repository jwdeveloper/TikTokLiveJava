package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.live.LiveClient;

public class GiftTestingExample {


    public static void main(String[] args) throws Exception {
        LiveClient client = TikTokLive.newClient(SimpleExample.TIKTOK_HOSTNAME)
                .configure(liveClientSettings ->
                {
                   // liveClientSettings.setOffline(true);
                })
                .onConnected((liveClient, event) ->
                {
                    liveClient.getLogger().info("Connected");
                })
                .onGiftCombo((liveClient, event) ->
                {

                })
                .onGift((liveClient, event) ->
                {
                    liveClient.getLogger().info("New fakeGift: " + event.getGift());
                })
                .buildAndConnect();

        var gifts = TikTokLive.gifts();
        var fakeGift = TikTokGiftEvent.of(gifts.getByName("Rose"));
        var fakeGift2 = TikTokGiftEvent.of("Rose", 1, 23);
        client.publishEvent(fakeGift);
        client.publishEvent(fakeGift2);
    }


    public void GetTesterBuilder() {

    }


    public void GetBuilder() {

    }
}
