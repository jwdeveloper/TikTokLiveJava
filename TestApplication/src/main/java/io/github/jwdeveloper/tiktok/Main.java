package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.events.messages.*;
import io.github.jwdeveloper.tiktok.live.LiveClient;

import java.io.IOException;

public class Main {

    public static String TEST_TIKTOK_USER = "vadimpyrography";

    public static void main(String[] args) throws IOException {
        var client = TikTokLive.newClient(TEST_TIKTOK_USER)
                .onConnected(Main::onConnected)
                .onDisconnected(Main::onDisconnected)
                .onRoomViewerData(Main::onViewerData)
                .onJoin(Main::onJoin)
                .onComment(Main::onComment)
                .onFollow(Main::onFollow)
                .onShare(Main::onShare)
                .onSubscribe(Main::onSubscribe)
                .onLike(Main::onLike)
                .onGiftMessage(Main::onGiftMessage)
                .onEmote(Main::onEmote)
                .onError((_client, error) ->
                {
                    error.getException().printStackTrace();
                })
                .buildAndRun();

        var viewers = client.getRoomInfo().getViewersCount();
        System.in.read();
    }

    private static void onConnected(LiveClient tikTokLive, TikTokConnectedEvent e) {
        print("Connected");
    }

    private static void onDisconnected(LiveClient tikTokLive, TikTokDisconnectedEvent e) {
        print("Disconnected");
    }

    private static void onViewerData(LiveClient tikTokLive, TikTokRoomViewerDataEvent e) {
        print("Viewer count is:", e.getViewerCount());
    }

    private static void onJoin(LiveClient tikTokLive, TikTokJoinEvent e) {
        print(e.getUser().getUniqueId(), "joined!");
    }

    private static void onComment(LiveClient tikTokLive, TikTokCommentEvent e) {
        print(e.getUser().getUniqueId(), e.getText());
    }

    private static void onFollow(LiveClient tikTokLive, TikTokFollowEvent e) {
        print(e.getNewFollower().getUniqueId(), "followed!");
    }

    private static void onShare(LiveClient tikTokLive, TikTokShareEvent e) {
        print(e.getUser().getUniqueId(), "shared!");
    }

    private static void onSubscribe(LiveClient tikTokLive, TikTokSubscribeEvent e) {
        print(e.getNewSubscriber().getUniqueId(), "subscribed!");
    }

    private static void onLike(LiveClient tikTokLive, TikTokLikeEvent e) {

        print(e.getSender().getUniqueId(), "liked!");
    }

    private static void onGiftMessage(LiveClient tikTokLive, TikTokGiftMessageEvent e)
    {
        print(e.getSender().getUniqueId(), "sent", e.getAmount(), "x", e.getGift().getName());
    }

    private static void onEmote(LiveClient tikTokLive, TikTokEmoteEvent e) {
        print(e.getUser().getUniqueId(), "sent", e.getEmoteId());
    }

    private static void print(Object... messages) {
        var sb = new StringBuilder();
        for (var message : messages) {
            sb.append(message).append(" ");
        }
        System.out.println(sb);
    }
}