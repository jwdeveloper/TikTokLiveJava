package io.github.jwdeveloper.tiktok;

import java.io.IOException;

public class SimpleExample {
    public static void main(String[] args) throws IOException {

        TikTokLive.newClient(Main.TEST_TIKTOK_USER)
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
                    System.out.println("OTO tajeminica wiary");
                    event.getException().printStackTrace();
                })
                .buildAndRun();
        System.in.read();
    }
}
