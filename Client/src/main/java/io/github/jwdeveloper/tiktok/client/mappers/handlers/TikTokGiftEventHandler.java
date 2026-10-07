/*
 * Copyright (c) 2023-2024 jwdeveloper jacekwoln@gmail.com
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
package io.github.jwdeveloper.tiktok.client.mappers.handlers;

import io.github.jwdeveloper.dependance.injector.api.annotations.Inject;
import io.github.jwdeveloper.tiktok.api.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.api.data.events.gift.*;
import io.github.jwdeveloper.tiktok.api.data.models.Picture;
import io.github.jwdeveloper.tiktok.api.data.models.gifts.*;
import io.github.jwdeveloper.tiktok.api.live.GiftsManager;
import io.github.jwdeveloper.tiktok.api.mappers.LiveMapperHelper;
import io.github.jwdeveloper.tiktok.api.mappers.data.MappingResult;
import io.github.jwdeveloper.tiktok.api.messages.webcast.WebcastGiftMessage;
import io.github.jwdeveloper.tiktok.client.TikTokRoomInfo;
import lombok.SneakyThrows;

import java.util.*;
import java.util.function.LongSupplier;

public class TikTokGiftEventHandler {
    /**
     * A streak that has not been finished within this window is closed by its last active frame.
     * TikTok does not guarantee a finishing frame (sendType 0) for every streak, and without this
     * fallback such a gift would never raise onGift at all.
     * <p>
     * Measured from the last frame of the streak rather than its start, so an ongoing streak keeps
     * extending it. Deliberately generous: streaks can run into the thousands and a lull between
     * frames must not close one early, or the gift is split in half and the real finishing frame
     * is then discarded as a duplicate.
     */
    private static final long DEFAULT_COMBO_TIMEOUT_MS = 300_000;

    /**
     * TikTok repeats the finishing frame of a streak under a fresh msgId within milliseconds.
     * Finishing frames arriving inside this window after a streak was closed are ignored.
     */
    private static final long FINALIZED_RETENTION_MS = 60_000;

    private final Map<String, ComboState> activeCombos;
    private final Map<String, Long> finalizedCombos;
    private final TikTokRoomInfo tikTokRoomInfo;
    private final GiftsManager giftsManager;
    private final long comboTimeoutMillis;
    private final LongSupplier clock;

    /**
     * The constructor the container resolves. Annotated because the clock-injecting overload below
     * makes this class ambiguous to the injector, which then refuses to register it at all.
     */
    @Inject
    public TikTokGiftEventHandler(GiftsManager giftsManager, TikTokRoomInfo tikTokRoomInfo) {
        this(giftsManager, tikTokRoomInfo, DEFAULT_COMBO_TIMEOUT_MS, System::currentTimeMillis);
    }

    public TikTokGiftEventHandler(GiftsManager giftsManager,
                                  TikTokRoomInfo tikTokRoomInfo,
                                  long comboTimeoutMillis,
                                  LongSupplier clock) {
        this.activeCombos = new HashMap<>();
        this.finalizedCombos = new HashMap<>();
        this.tikTokRoomInfo = tikTokRoomInfo;
        this.giftsManager = giftsManager;
        this.comboTimeoutMillis = comboTimeoutMillis;
        this.clock = clock;
    }

    @SneakyThrows
    public MappingResult handleGifts(byte[] msg, String name, LiveMapperHelper helper) {
        var currentMessage = WebcastGiftMessage.parseFrom(msg);
        var gifts = handleGift(currentMessage);
        return MappingResult.of(currentMessage, gifts);
    }

    public List<TikTokEvent> handleGift(WebcastGiftMessage currentMessage) {
        var now = clock.getAsLong();
        var events = new ArrayList<>(flushTimedOutCombos(now));

        //If gift is not streakable just return onGift event
        if (currentMessage.getGift().getType() != 1) {
            events.add(getGiftComboEvent(currentMessage, GiftComboStateType.Finished));
            events.add(getGiftEvent(currentMessage));
            return events;
        }

        var key = comboKey(currentMessage);
        var currentType = GiftComboStateType.fromNumber(currentMessage.getSendType());

        if (currentType == GiftComboStateType.Active) {
            var previous = activeCombos.put(key, new ComboState(currentMessage, now));
            events.add(getGiftComboEvent(currentMessage,
                    previous == null ? GiftComboStateType.Begin : GiftComboStateType.Active));
            return events;
        }

        //TikTok may repeat the finishing frame of a streak under a fresh msgId. Without this guard
        //every repeat raises another onGift and the gift gets counted twice.
        var finalizedUntil = finalizedCombos.get(key);
        if (finalizedUntil != null && finalizedUntil > now)
            return events;

        var previous = activeCombos.remove(key);
        finalizedCombos.put(key, now + FINALIZED_RETENTION_MS);
        if (previous != null)
            events.add(getGiftComboEvent(currentMessage, GiftComboStateType.Finished));
        events.add(getGiftEvent(currentMessage));
        return events;
    }

    /**
     * Closes streaks that timed out waiting for their finishing frame. Runs lazily on every
     * incoming gift, so a streak left open at the very end of a live may still be missed.
     */
    private List<TikTokEvent> flushTimedOutCombos(long now) {
        finalizedCombos.values().removeIf(expiresAt -> expiresAt <= now);
        if (activeCombos.isEmpty())
            return List.of();

        var events = new ArrayList<TikTokEvent>();
        var iterator = activeCombos.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            var state = entry.getValue();
            if (now - state.updatedAt() < comboTimeoutMillis)
                continue;

            iterator.remove();
            finalizedCombos.put(entry.getKey(), now + FINALIZED_RETENTION_MS);
            events.add(getGiftComboEvent(state.message(), GiftComboStateType.Finished));
            events.add(getGiftEvent(state.message()));
        }
        return events;
    }

    /**
     * Identifies a single streak. Keying on the user alone lets concurrent streaks of the same
     * user overwrite each other, and made one finishing frame wipe the state of every other user.
     */
    private String comboKey(WebcastGiftMessage message) {
        var groupId = message.getGroupId() != 0
                ? Long.toString(message.getGroupId())
                : message.getOrderId();
        if (groupId == null || groupId.isEmpty())
            groupId = Long.toString(message.getCommon().getMsgId());

        return message.getUser().getId() + ":" + message.getGiftId() + ":" + groupId;
    }

    private record ComboState(WebcastGiftMessage message, long updatedAt) {
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