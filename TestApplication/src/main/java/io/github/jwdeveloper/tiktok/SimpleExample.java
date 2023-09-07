package io.github.jwdeveloper.tiktok;
import java.io.IOException;

public class SimpleExample {
    public static void main(String[] args) throws IOException {

        // set tiktok username
        TikTokLive.newClient(Main.TEST_TIKTOK_USER)
                .configure(clientSettings ->
                {

                })
                .onFollow((liveClient, event) ->
                {
                    System.out.println("Follow joined -> " + event.getNewFollower().getNickName());
                })
                .onConnected((client, event) ->
                {
                    System.out.println("Connected");
                })
                .onJoin((client, event)  ->
                {
                    System.out.println("User joined -> " + event.getUser().getNickName());
                })
                .onComment((client, event)  ->
                {
                   System.out.println(event.getUser().getUniqueId() + ": " + event.getText());
                })
                .onEvent((client, event) ->
                {
                    System.out.println("Viewers count: "+client.getRoomInfo().getViewersCount());
                })
                .onError((client, event)  ->
                {
                    event.getException().printStackTrace();
                })
                .buildAndRun();
        System.in.read();
    }
}
