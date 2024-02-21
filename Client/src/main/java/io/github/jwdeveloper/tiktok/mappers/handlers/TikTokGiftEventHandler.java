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
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftOld;
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftSendType;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.live.GiftManager;
import io.github.jwdeveloper.tiktok.mappers.TikTokMapperHelper;
import io.github.jwdeveloper.tiktok.mappers.data.MappingResult;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastGiftMessage;
import lombok.SneakyThrows;
import sun.misc.Unsafe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TikTokGiftEventHandler {
    private final GiftManager giftManager;
    private final Map<Long, WebcastGiftMessage> giftsMessages;
    private final TikTokRoomInfo tikTokRoomInfo;

    public TikTokGiftEventHandler(GiftManager giftManager, TikTokRoomInfo tikTokRoomInfo) {
        this.giftManager = giftManager;
        giftsMessages = new HashMap<>();
        this.tikTokRoomInfo = tikTokRoomInfo;
    }

    @SneakyThrows
    public MappingResult handleGifts(byte[] msg, String name, TikTokMapperHelper helper) {
        var currentMessage = WebcastGiftMessage.parseFrom(msg);
        var gifts = handleGift(currentMessage);
        return MappingResult.of(currentMessage, gifts);
    }

    public List<TikTokEvent> handleGift(WebcastGiftMessage currentMessage) {
        var userId = currentMessage.getUser().getId();
        var currentType = GiftSendType.fromNumber(currentMessage.getSendType());
        var containsPreviousMessage = giftsMessages.containsKey(userId);


        //If gift is not streakable just return onGift event
        if (currentMessage.getGift().getType() != 1) {
            var comboEvent = getGiftComboEvent(currentMessage, GiftSendType.Finished);
            var giftEvent = getGiftEvent(currentMessage);
            return List.of(comboEvent, giftEvent);
        }

        if (!containsPreviousMessage) {
            if (currentType == GiftSendType.Finished) {
                return List.of(getGiftEvent(currentMessage));
            } else {
                giftsMessages.put(userId, currentMessage);
                return List.of(getGiftComboEvent(currentMessage, GiftSendType.Begin));
            }
        }

        var previousMessage = giftsMessages.get(userId);
        var previousType = GiftSendType.fromNumber(previousMessage.getSendType());
        if (currentType == GiftSendType.Active &&
                previousType == GiftSendType.Active) {
            giftsMessages.put(userId, currentMessage);
            return List.of(getGiftComboEvent(currentMessage, GiftSendType.Active));
        }


        if (currentType == GiftSendType.Finished &&
                previousType == GiftSendType.Active) {
            giftsMessages.clear();
            return List.of(
                    getGiftComboEvent(currentMessage, GiftSendType.Finished),
                    getGiftEvent(currentMessage));
        }

        return List.of();
    }


    private TikTokGiftEvent getGiftEvent(WebcastGiftMessage message) {
        var gift = getGiftObject(message);
        return new TikTokGiftEvent(gift, tikTokRoomInfo.getHost(), message);
    }

    private TikTokGiftEvent getGiftComboEvent(WebcastGiftMessage message, GiftSendType state) {
        var gift = getGiftObject(message);
        return new TikTokGiftComboEvent(gift, tikTokRoomInfo.getHost(), message, state);
    }

    private GiftOld getGiftObject(WebcastGiftMessage giftMessage) {
        var giftId = (int) giftMessage.getGiftId();
        var gift = giftManager.findById(giftId);
        if (gift == GiftOld.UNDEFINED) {
            gift = giftManager.findByName(giftMessage.getGift().getName());
        }
        if (gift == GiftOld.UNDEFINED) {
            gift = giftManager.registerGift(
                    giftId,
                    giftMessage.getGift().getName(),
                    giftMessage.getGift().getDiamondCount(),
                    Picture.map(giftMessage.getGift().getImage()));
        }

        if (gift.getPicture().getLink().endsWith(".webp")) {
            updatePicture(gift, giftMessage);
        }
        return gift;
    }


    private void updatePicture(GiftOld gift, WebcastGiftMessage webcastGiftMessage) {
        try {
            var picture = Picture.map(webcastGiftMessage.getGift().getImage());
            var constructor = Unsafe.class.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            var field = GiftOld.class.getDeclaredField("picture");
            field.setAccessible(true);
            field.set(gift, picture);
        } catch (Exception e) {
            throw new TikTokLiveException("Unable to update picture in gift: " + gift.toString());
        }
    }
}
