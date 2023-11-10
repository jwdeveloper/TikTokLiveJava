package io.github.jwdeveloper.tiktok.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TikTokLiveOnlineCheckerTest {

    private final String TARGET_USER = "bangbetmenygy";

    @Test
    public void shouldTestOnline() {
        var sut = new TikTokLiveOnlineChecker();
        var result = sut.isOnline(TARGET_USER);

        Assertions.assertTrue(result);
    }

}