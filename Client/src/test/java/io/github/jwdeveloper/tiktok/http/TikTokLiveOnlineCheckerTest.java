package io.github.jwdeveloper.tiktok.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TikTokLiveOnlineCheckerTest {

    private final String TARGET_USER = "bangbetmenygy";

    @Test
    public void shouldTestOnline() {
        var sut = new TikTokDataChecker();
        var result = sut.isOnline(TARGET_USER);

        Assertions.assertTrue(result);
    }

}