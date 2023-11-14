package io.github.jwdeveloper.tiktok.http;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TikTokLiveOnlineCheckerTest {

    public boolean enableTests = false;

    @Test
    public void shouldTestOnline() {

        if(!enableTests)
        {
            return;
        }

        var TARGET_USER = "bangbetmenygy";
        var sut = new TikTokDataChecker();
        var result = sut.isOnline(TARGET_USER);

        Assertions.assertTrue(result);
    }

    @Test
    public void shouldBeOffline() {

        var TARGET_USER = "dostawcavideo";
        var sut = new TikTokDataChecker();
        var result = sut.isOnline(TARGET_USER);

        Assertions.assertFalse(result);
    }

    @Test
    public void shouldBeValid() {

        var TARGET_USER = "dostawcavideo";
        var sut = new TikTokDataChecker();
        var result = sut.isHostNameValid(TARGET_USER);

        Assertions.assertTrue(result);
    }

    @Test
    public void shouldNotBeValid() {
        var TARGET_USER = "adadsdadadadadadadadddasdadsafafsafsafas";
        var sut = new TikTokDataChecker();
        var result = sut.isHostNameValid(TARGET_USER);

        Assertions.assertFalse(result);
    }

}