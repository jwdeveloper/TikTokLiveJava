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
package io.github.jwdeveloper.tiktok.gifts;

import io.github.jwdeveloper.tiktok.data.models.Gift;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)

public class TikTokGiftManagerTest {

    @InjectMocks
    TikTokGiftManager giftManager;

    private static final Picture rosePicture = new Picture("https://p19-webcast.tiktokcdn.com/img/maliva/webcast-va/eba3a9bb85c33e017f3648eaf88d7189~tplv-obj.png");

    @Test
    void registerGift() {
        var fakeGift = giftManager.registerGift(123, "Fake gift", 123123, rosePicture);
        var gifts = giftManager.getGifts();
        var optional = gifts.stream().filter(r -> r == fakeGift).findFirst();
        Assertions.assertTrue(optional.isPresent());
    }

    @Test
    void findById() {
        var target = giftManager.registerGift(123, "FAKE", 123123, rosePicture);
        var result = giftManager.findById(target.getId());
        Assertions.assertEquals(target, result);
    }

    @Test
    void findByName() {
        var target = giftManager.registerGift(123, "FAKE", 123123, rosePicture);
        var result = giftManager.findByName(target.getName());
        Assertions.assertEquals(target, result);
    }

    @Test
    void getGifts() {
        Assertions.assertEquals(Gift.values().length, giftManager.getGifts().size());
    }


}