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
import io.github.jwdeveloper.tiktok.data.events.gift.*;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.data.models.gifts.*;
import io.github.jwdeveloper.tiktok.live.GiftsManager;
import io.github.jwdeveloper.tiktok.mappers.LiveMapperHelper;
import io.github.jwdeveloper.tiktok.mappers.data.MappingResult;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastGiftMessage;
import lombok.SneakyThrows;

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
    public MappingResult handleGifts(byte[] msg, String name, LiveMapperHelper helper) {
        var currentMessage = WebcastGiftMessage.parseFrom(msg);
        var gifts = handleGift(currentMessage);
        return MappingResult.of(currentMessage, gifts);
    }

    public List<TikTokEvent> handleGift(WebcastGiftMessage currentMessage) {
        //If gift is not streakable just return onGift event
        if (currentMessage.getGift().getType() != 1) {
            var comboEvent = getGiftComboEvent(currentMessage, GiftComboStateType.Finished);
            var giftEvent = getGiftEvent(currentMessage);
            return List.of(comboEvent, giftEvent);
        }

        var userId = currentMessage.getUser().getId();
        var currentType = GiftComboStateType.fromNumber(currentMessage.getSendType());
        var previousMessage = giftsMessages.get(userId);

        if (previousMessage == null) {
            if (currentType == GiftComboStateType.Finished) {
                return List.of(getGiftEvent(currentMessage));
            } else {
                giftsMessages.put(userId, currentMessage);
                return List.of(getGiftComboEvent(currentMessage, GiftComboStateType.Begin));
            }
        }

        var previousType = GiftComboStateType.fromNumber(previousMessage.getSendType());
        if (currentType == GiftComboStateType.Active &&
                previousType == GiftComboStateType.Active) {
            giftsMessages.put(userId, currentMessage);
            return List.of(getGiftComboEvent(currentMessage, GiftComboStateType.Active));
        }


        if (currentType == GiftComboStateType.Finished &&
                previousType == GiftComboStateType.Active) {
            giftsMessages.clear();
            return List.of(
                    getGiftComboEvent(currentMessage, GiftComboStateType.Finished),
                    getGiftEvent(currentMessage));
        }

        return List.of();
    }


    private TikTokGiftEvent getGiftEvent(WebcastGiftMessage message) {
        var gift = getGiftObject(message);
        return new TikTokGiftEvent(gift, tikTokRoomInfo.getHost(), message);
    }

    private TikTokGiftEvent getGiftComboEvent(WebcastGiftMessage message, GiftComboStateType state) {
        var gift = getGiftObject(message);
        return new TikTokGiftComboEvent(gift, tikTokRoomInfo.getHost(), message, state);
    }

    private Gift getGiftObject(WebcastGiftMessage giftMessage) {
        var giftId = (int) giftMessage.getGiftId();
        var gift = giftsManager.getById(giftId);
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
            gift.setPicture(Picture.map(giftMessage.getGift().getImage()));

        return gift;
    }
}