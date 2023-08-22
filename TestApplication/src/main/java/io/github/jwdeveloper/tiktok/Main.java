package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.events.messages.*;

import java.io.IOException;

public class Main {

    public static String TEST_USER_SUBJECT = "stiflerhub";

    public static void main(String[] args) throws IOException {
        var client = TikTokLive.newClient(TEST_USER_SUBJECT)
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
                .onError(tikTokErrorEvent ->
                {
                    //  tikTokErrorEvent.getException().printStackTrace();
                })
                .buildAndRun();


        var viewers = client.getRoomInfo().getViewersCount();
        System.in.read();
    }

    private static void onConnected(TikTokConnectedEvent e) {
        print("Connected");
    }

    private static void onDisconnected(TikTokDisconnectedEvent e) {
        print("Disconnected");
    }

    private static void onViewerData(TikTokRoomViewerDataEvent e) {
        print("Viewer count is:", e.getViewerCount());
    }

    private static void onJoin(TikTokJoinEvent e) {
        print(e.getUser().getUniqueId(), "joined!");
    }

    private static void onComment(TikTokCommentEvent e) {
        print(e.getUser().getUniqueId(), e.getText());
    }

    private static void onFollow(TikTokFollowEvent e) {
        print(e.getNewFollower().getUniqueId(), "followed!");
    }

    private static void onShare(TikTokShareEvent e) {
        print(e.getUser().getUniqueId(), "shared!");
    }

    private static void onSubscribe(TikTokSubscribeEvent e) {
        print(e.getNewSubscriber().getUniqueId(), "subscribed!");
    }

    private static void onLike(TikTokLikeEvent e) {

        print(e.getSender().getUniqueId(), "liked!");
    }

    private static void onGiftMessage(TikTokGiftMessageEvent e) {
        print(e.getSender().getUniqueId(), "sent", e.getAmount(), "x", e.getGift().getName());
    }

    private static void onEmote(TikTokEmoteEvent e) {
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