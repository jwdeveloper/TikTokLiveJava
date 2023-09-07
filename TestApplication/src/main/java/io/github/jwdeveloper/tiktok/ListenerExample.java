package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.annotations.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.events.messages.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokGiftMessageEvent;
import io.github.jwdeveloper.tiktok.listener.TikTokEventListener;
import io.github.jwdeveloper.tiktok.live.LiveClient;

import java.io.IOException;

public class ListenerExample
{
    public static void main(String[] args) throws IOException {

        CustomListener customListener = new CustomListener();

        // set tiktok username
        TikTokLive.newClient(Main.TEST_TIKTOK_USER)
                .addListener(customListener)
                .buildAndRun();

        System.in.read();
    }

    public static class CustomListener implements TikTokEventListener
    {

        @TikTokEventHandler
        public void onError(LiveClient liveClient, TikTokErrorEvent event)
        {
            System.out.println(event.getException().getMessage());
        }

        @TikTokEventHandler
        public void onCommentMessage(LiveClient liveClient, TikTokCommentEvent event)
        {
            System.out.println(event.getText());
        }

        @TikTokEventHandler
        public void onGiftMessage(LiveClient liveClient, TikTokGiftMessageEvent event)
        {
            System.out.println(event.getGift().getDescription());
        }
    }
}
