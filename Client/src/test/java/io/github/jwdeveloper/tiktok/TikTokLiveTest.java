package io.github.jwdeveloper.tiktok;

import org.junit.jupiter.api.Test;

import java.io.IOException;

public class TikTokLiveTest
{
    public static String TEST_USER_SUBJECT = "live_interactive_";


    @Test
    public void ShouldConnect() throws IOException {
        var client = TikTokLive.newClient(TEST_USER_SUBJECT).build();
        client.run();



        System.in.read();

    }

}