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
package io.github.jwdeveloper.tiktok.mappers.handlers;

import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftComboEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftComboStateType;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.live.GiftsManager;
import io.github.jwdeveloper.tiktok.mappers.TikTokMapperHelper;
import io.github.jwdeveloper.tiktok.mappers.data.MappingResult;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastGiftMessage;
import lombok.SneakyThrows;
import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

public class TikTokGiftEventHandler {
    private final Map<Long, WebcastGiftMessage> giftsMessages;
    private final TikTokRoomInfo tikTokRoomInfo;

    private final GiftsManager giftsManager;

    public TikTokGiftEventHandler(GiftsManager giftsManager, TikTokRoomInfo tikTokRoomInfo) {
        giftsMessages = new HashMap<>();
        this.tikTokRoomInfo = tikTokRoomInfo;
        this.giftsManager = giftsManager;
    }

    @SneakyThrows
    public MappingResult handleGifts(byte[] msg, String name, TikTokMapperHelper helper) {
        WebcastGiftMessage currentMessage = WebcastGiftMessage.parseFrom(msg);
        List<TikTokEvent> gifts = handleGift(currentMessage);
        return MappingResult.of(currentMessage, gifts);
    }

    public List<TikTokEvent> handleGift(WebcastGiftMessage currentMessage) {
        long userId = currentMessage.getUser().getId();
        GiftComboStateType currentType = GiftComboStateType.fromNumber(currentMessage.getSendType());
        boolean containsPreviousMessage = giftsMessages.containsKey(userId);


        //If gift is not streakable just return onGift event
        if (currentMessage.getGift().getType() != 1) {
            TikTokGiftEvent comboEvent = getGiftComboEvent(currentMessage, GiftComboStateType.Finished);
            TikTokGiftEvent giftEvent = getGiftEvent(currentMessage);
            return Arrays.asList(comboEvent, giftEvent);
        }

        if (!containsPreviousMessage) {
            if (currentType == GiftComboStateType.Finished) {
                return Collections.singletonList(getGiftEvent(currentMessage));
            } else {
                giftsMessages.put(userId, currentMessage);
                return Collections.singletonList(getGiftComboEvent(currentMessage, GiftComboStateType.Begin));
            }
        }

        WebcastGiftMessage previousMessage = giftsMessages.get(userId);
        GiftComboStateType previousType = GiftComboStateType.fromNumber(previousMessage.getSendType());
        if (currentType == GiftComboStateType.Active &&
                previousType == GiftComboStateType.Active) {
            giftsMessages.put(userId, currentMessage);
            return Collections.singletonList(getGiftComboEvent(currentMessage, GiftComboStateType.Active));
        }


        if (currentType == GiftComboStateType.Finished &&
                previousType == GiftComboStateType.Active) {
            giftsMessages.clear();
            return Arrays.asList(
                    getGiftComboEvent(currentMessage, GiftComboStateType.Finished),
                    getGiftEvent(currentMessage));
        }

        return Collections.emptyList();
    }


    private TikTokGiftEvent getGiftEvent(WebcastGiftMessage message) {
        Gift gift = getGiftObject(message);
        return new TikTokGiftEvent(gift, tikTokRoomInfo.getHost(), message);
    }

    private TikTokGiftEvent getGiftComboEvent(WebcastGiftMessage message, GiftComboStateType state) {
        Gift gift = getGiftObject(message);
        return new TikTokGiftComboEvent(gift, tikTokRoomInfo.getHost(), message, state);
    }

    private Gift getGiftObject(WebcastGiftMessage giftMessage) {
        int giftId = (int) giftMessage.getGiftId();
        Gift gift = giftsManager.getById(giftId);
        if (gift == Gift.UNDEFINED)
            gift = giftsManager.getByName(giftMessage.getGift().getName());
        if (gift == Gift.UNDEFINED) {
            gift = new Gift(giftId,
                    giftMessage.getGift().getName(),
                    giftMessage.getGift().getDiamondCount(),
                    Picture.map(giftMessage.getGift().getImage()));

            giftsManager.attachGift(gift);
        }

        if (gift.getPicture().getLink().endsWith(".webp"))
        {
            updatePicture(gift, giftMessage);
        }

        return gift;
    }

    // TODO-kohlerpop1: I do not think this method is needed for any reason?
    // TODO response:

    /**
     * Some generated gifts in JSON file contains .webp image format,
     * that's bad since java by the defult is not supporing .webp and when URL is
     * converted to Java.io.Image then image is null
     *
     * However, TikTok in GiftWebcast event always has image in .jpg format,
     * so I take advantage of it and swap .webp url with .jpg url
     *
     */

    private void updatePicture(Gift gift, WebcastGiftMessage webcastGiftMessage) {
        try {
            Picture picture = Picture.map(webcastGiftMessage.getGift().getImage());
            Constructor<?> constructor = Unsafe.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            Field field = Gift.class.getDeclaredField("picture");
            field.setAccessible(true);
            field.set(gift, picture);
        } catch (Exception e) {
            throw new TikTokLiveException("Unable to update picture in gift: " + gift.toString());
        }
    }
}
