package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.annotations.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokGiftMessageEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.listener.TikTokEventListener;
import io.github.jwdeveloper.tiktok.live.LiveClient;

import java.io.IOException;

public class ListenerExample
{
    public static void main(String[] args) throws IOException {

        CustomListener customListener = new CustomListener();

        // set tiktok username
        var client = TikTokLive.newClient(Main.TEST_TIKTOK_USER)
                .addListener(customListener)
                .buildAndRun();

        System.in.read();
    }

    /*
       Method in TikTokEventListener should meet 4 requirements to be detected
        - must have @TikTokEventHandler annotation
        - must have 2 parameters
        - first parameter must be LiveClient
        - second must be class that extending TikTokEvent
     */
    public static class CustomListener implements TikTokEventListener
    {

        @TikTokEventHandler
        public void onLike(LiveClient liveClient, TikTokLikeEvent event)
        {
            System.out.println(event.toString());
        }

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

        @TikTokEventHandler
        public void onAnyEvent(LiveClient liveClient, TikTokEvent event)
        {
            System.out.println(event.getClass().getSimpleName());
        }

    }
}
