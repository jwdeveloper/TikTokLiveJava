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
package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.live.GiftsManager;

import java.util.List;
import java.util.Map;

public class GiftsExample {

    public static void main(String[] args) {
        GiftsManager giftsManager = TikTokLive.gifts();



        List<Gift> giftsList = giftsManager.toList();
        for (Gift gift : giftsList) {
            System.out.println("Gift: " + gift);
        }

        Map<Integer, Gift> giftsMap = giftsManager.toMap();
        for (Map.Entry<Integer, Gift> entry : giftsMap.entrySet()) {
            System.out.println("GiftId: " + entry.getKey() + " Gift: " + entry.getValue());
        }

        System.out.println("total number of gifts: " + giftsManager.toList().size());

        Gift giftRose = giftsManager.getById(5655);
        Gift giftRoseByName = giftsManager.getByName("Rose");
        Gift giftByFilter = giftsManager.getByFilter(e -> e.getDiamondCost() > 50);

        List<Gift> giftsByFilter = giftsManager.getManyByFilter(e -> e.getDiamondCost() > 100);
        System.out.println("total number of gifts with cost higher then 100: " + giftsByFilter.size());
        /**
         * In case searched gift not exists getByName returns you Gift.UNDEFINED
         */
        Gift undefiedGift = giftsManager.getByName("GIFT WITH WRONG NAME");


        Gift customGift = new Gift(123213213, "Custom gift", 50, "https://images.pexels.com/photos/2071882/pexels-photo-2071882.jpeg?cs=srgb&dl=pexels-wojciech-kumpicki-2071882.jpg&fm=jpg");
        giftsManager.attachGift(customGift);


    }
}
