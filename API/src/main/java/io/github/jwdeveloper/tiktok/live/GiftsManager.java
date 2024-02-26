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

import com.google.gson.JsonObject;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.gifts.*;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

public interface GiftsManager {

    /**
     * You can create and attach your own custom gift to manager
     *
     * @param gift
     */
    void attachGift(Gift gift);

    /**
     * You can create and attach your own custom gift to manager
     *
     * @param gifts
     */
    void attachGiftsList(List<Gift> gifts);

    /**
     * finds gift by name
     * When gift not found return Gift.UNDEFINED;
     *
     * @param name gift name
     */
    Gift getByName(String name);

    /**
     * finds gift by id
     * When gift not found return Gift.UNDEFINED;
     *
     * @param giftId giftId
     */
    Gift getById(int giftId);


    /**
     * finds gift by filter
     * When gift not found return Gift.UNDEFINED;
     */
    Gift getByFilter(Predicate<Gift> filter);

    List<Gift> getManyByFilter(Predicate<Gift> filter);

    /**
     * @return list of all gifts
     */
    List<Gift> toList();


    /**
     * @return list of all map of all gifts where Integer is gift Id
     */
    Map<Integer, Gift> toMap();
}