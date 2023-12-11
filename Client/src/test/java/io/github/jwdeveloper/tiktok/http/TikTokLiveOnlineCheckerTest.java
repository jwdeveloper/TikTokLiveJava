/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.http;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastGiftMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Random;

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

        var TARGET_USER = "karacomparetto";
        var sut = new TikTokDataChecker();
        var result = sut.isOnline(TARGET_USER);

        Assertions.assertFalse(result);
    }

    @Test
    public void shouldBeValid() throws InterruptedException {

        var TARGET_USER = "dostawcavideo";
        var sut = new TikTokDataChecker();
        var result = sut.isHostNameValid(TARGET_USER);


        TikTokLive.newClient("asdasd")
                        .onWebsocketResponse((liveClient, event) ->
                        {
                            for(var message : event.getResponse().getMessagesList())
                            {
                                if(message.getMethod().equals("WebcastGiftMessage"))
                                {
                                    try
                                    {
                                        var bytes = message.getMethodBytes();
                                        var rawMessage = WebcastGiftMessage.parseFrom(bytes);
                                        var giftName =rawMessage.getGift().getName();
                                    }
                                    catch (Exception e)
                                    {
                                        e.printStackTrace();
                                    }

                                }
                            }
                        });

        Assertions.assertTrue(result);
    }

    @Test
    public void shouldNotBeValid() {
        var TARGET_USER = "dqagdagda , asdaaasd";
        var sut = new TikTokDataChecker();
        var result = sut.isHostNameValid(TARGET_USER);

        Assertions.assertFalse(result);
    }

}