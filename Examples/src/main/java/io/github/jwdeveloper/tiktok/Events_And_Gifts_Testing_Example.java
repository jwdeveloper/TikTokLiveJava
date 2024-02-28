package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokSubNotifyEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokSubscribeEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftComboEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftComboStateType;
import io.github.jwdeveloper.tiktok.live.LiveClient;

public class Events_And_Gifts_Testing_Example {
    public static void main(String[] args) {
        LiveClient client = TikTokLive.newClient(ConnectionExample.TIKTOK_HOSTNAME)
                .configure(liveClientSettings ->
                {
                    liveClientSettings.setOffline(true);
                    liveClientSettings.setPrintToConsole(true);
                })
                .onConnected((liveClient, event) ->
                {
                    liveClient.getLogger().info("Connected");
                })
                .onDisconnected((liveClient, event) ->
                {
                    liveClient.getLogger().info("Disconnected");
                })
                .onGiftCombo((liveClient, event) ->
                {
                    liveClient.getLogger().info("New fake combo Gift: " + event.getGift());
                })
                .onGift((liveClient, event) ->
                {
                    liveClient.getLogger().info("New fake Gift: " + event.getGift());
                })
                .build();

        var gifts = TikTokLive.gifts();
        var roseGift = gifts.getByName("Rose");

        var fakeGift = TikTokGiftEvent.of(roseGift);
        var fakeComboGift = TikTokGiftComboEvent.of(roseGift, 12, GiftComboStateType.Begin);

        var fakeMessage = TikTokCommentEvent.of("Mark", "Hello world");

        var fakeSubscriber = TikTokSubscribeEvent.of("Mark");
        var fakeFollow = TikTokFollowEvent.of("Mark");
        var fakeLike = TikTokLikeEvent.of("Mark", 12);
        var fakeJoin = TikTokJoinEvent.of("Mark");

        client.connect();

        client.publishEvent(fakeGift);
        client.publishEvent(fakeComboGift);
        client.publishEvent(fakeMessage);
        client.publishEvent(fakeSubscriber);
        client.publishEvent(fakeFollow);
        client.publishEvent(fakeLike);
        client.publishEvent(fakeJoin);

        client.disconnect();
    }


}
