package io.github.jwdeveloper.tiktok;

import java.io.IOException;

public class SimpleExample {
    public static void main(String[] args) throws IOException {
        // Username of someone who is currently live
        var tiktokUsername = "szwagierkaqueen";

        TikTokLive.newClient(tiktokUsername)
                .configure(settings ->
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
        System.in.read();
    }
}
