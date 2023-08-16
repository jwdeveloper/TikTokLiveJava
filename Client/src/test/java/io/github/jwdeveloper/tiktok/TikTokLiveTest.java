package io.github.jwdeveloper.tiktok;


import io.github.jwdeveloper.tiktok.events.messages.*;
import org.junit.Test;

import java.io.IOException;

public class TikTokLiveTest {
    public static String TEST_USER_SUBJECT = "tv_asahi_news";


    @Test
    public void ShouldConnect() throws IOException {
        var client = TikTokLive.newClient(TEST_USER_SUBJECT)
                .onConnected(this::onConnected)
                .onDisconnected(this::onDisconnected)
                .onRoomViewerData(this::onViewerData)
                .onJoin(this::onJoin)
                .onComment(this::onComment)
                .onFollow(this::onFollow)
                .onShare(this::onShare)
                .onSubscribe(this::onSubscribe)
                .onLike(this::onLike)
                .onGiftMessage(this::onGiftMessage)
                .onEmote(this::onEmote)
                .buildAndRun();
        System.in.read();

    }
    private void onConnected(TikTokConnectedEvent e) {
        print("Connected");
    }

    private void onDisconnected(TikTokDisconnectedEvent e) {
        print("Disconnected");
    }

    private void onViewerData(TikTokRoomViewerDataEvent e) {
        print("Viewer count is:", e.getViewerCount());
    }

    private void onJoin(TikTokJoinEvent e) {
        print(e.getUser().getUniqueId(), "joined!");
    }

    private void onComment(TikTokCommentEvent e) {
        print(e.getUser().getUniqueId(), e.getText());
    }

    private void onFollow(TikTokFollowEvent e) {
        print(e.getNewFollower().getUniqueId(), "followed!");
    }

    private void onShare(TikTokShareEvent e) {
        print(e.getUser().getUniqueId(), "shared!");
    }

    private void onSubscribe(TikTokSubscribeEvent e) {
        print(e.getNewSubscriber().getUniqueId(), "subscribed!");
    }

    private void onLike(TikTokLikeEvent e) {

        print(e.getSender().getUniqueId(), "liked!");
    }

    private void onGiftMessage(TikTokGiftMessageEvent e) {
        print(e.getSender().getUniqueId(), "sent", e.getAmount(), "x", e.getGift().getName());
    }

    private void onEmote(TikTokEmoteEvent e) {
        print(e.getUser().getUniqueId(), "sent", e.getEmoteId());
    }

    private static void print(Object... messages) {
        var sb = new StringBuilder();
        for (var message : messages) {
            sb.append(message).append(" ");
        }
        System.out.println(sb.toString());
    }

}