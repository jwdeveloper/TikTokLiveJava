package io.github.jwdeveloper.tiktok;

public class SimpleExample {
    public static void main(String[] args) {
        // Username of someone who is currently live
        var tiktokUsername = "officialgeilegisela";

        TikTokLive.newClient(tiktokUsername)
                .clientSettings(settings ->
                {
                })
                .onConnected(event ->
                {
                    System.out.println("Connected");
                })
                .onJoin(event ->
                {
                    System.out.println("User joined -> " + event.getUser().getNickName());
                })
                .onComment(event ->
                {
                    System.out.println(event.getUser().getUniqueId() + ": " + event.getText());
                })
                .onError(event ->
                {
                    event.getException().printStackTrace();
                })
                .buildAndRun();
    }
}
