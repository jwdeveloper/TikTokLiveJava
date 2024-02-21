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
package io.github.jwdeveloper.tiktok.live;

import io.github.jwdeveloper.tiktok.data.models.gifts.GiftOld;
import io.github.jwdeveloper.tiktok.data.models.Picture;

import java.util.List;

public interface GiftManager {

    /**
     * In case you can't find your gift in Gift enum. You can register gift
     * manually here to make it detected while TikTokGiftEvent
     *
     * @param id gift's id
     * @param name gift's name
     * @param diamondCost diamond cost
     * @return
     */
    GiftOld registerGift(int id, String name, int diamondCost, Picture picture);


    /**
     *
     * @param giftId
     * @return
     */
    GiftOld findById(int giftId);

    /**
     *
     * @param giftName
     * @return
     */
    GiftOld findByName(String giftName);

    /**
     *
     * @return all gifts
     */
    List<GiftOld> getGifts();
}
