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
package io.github.jwdeveloper.tiktok.client.handlers.events;

import io.github.jwdeveloper.tiktok.client.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.api.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.api.data.events.gift.TikTokGiftComboEvent;
import io.github.jwdeveloper.tiktok.api.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.api.data.models.Picture;
import io.github.jwdeveloper.tiktok.api.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.api.data.models.gifts.GiftComboStateType;
import io.github.jwdeveloper.tiktok.client.gifts.TikTokGiftsManager;
import io.github.jwdeveloper.tiktok.client.mappers.handlers.TikTokGiftEventHandler;
import io.github.jwdeveloper.tiktok.api.messages.data.Image;
import io.github.jwdeveloper.tiktok.api.messages.data.User;
import io.github.jwdeveloper.tiktok.api.messages.webcast.WebcastGiftMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TikTokGiftEventHandlerTest {

    private static final long COMBO_TIMEOUT_MS = 30_000;

    public TikTokGiftEventHandler handler;
    private AtomicLong clock;

    @BeforeEach
    public void before() {
        var manager = new TikTokGiftsManager(List.of());
        var info = new TikTokRoomInfo();
        info.setHost(new io.github.jwdeveloper.tiktok.api.data.models.users.User(123L, "test", new Picture("")));
        manager.attachGift(new Gift(123, "example", 123, "image.webp"));
        clock = new AtomicLong(1_000L);
        handler = new TikTokGiftEventHandler(manager, info, COMBO_TIMEOUT_MS, clock::get);
    }

    @Test
    void shouldHandleGifts() {
        var message = getGiftMessage("example-new-name", 123, "image-new.png", 0, 1, false);
        var result = handler.handleGift(message);

        Assertions.assertEquals(2, result.size());

        var event = (TikTokGiftEvent) result.get(0);
        var gift = event.getGift();
        Assertions.assertEquals("image-new.png", gift.getPicture().getLink());
        Assertions.assertEquals(123, gift.getId());
    }

    @Test
    void shouldHandleStrakableGift() {
        var message = getGiftMessage("example-new-name", 123, "image-new.png", 0, 1, true);
        var result = handler.handleGift(message);

        Assertions.assertEquals(1, result.size());

        var event = (TikTokGiftEvent) result.get(0);
        var gift = event.getGift();
        Assertions.assertEquals("image-new.png", gift.getPicture().getLink());
        Assertions.assertEquals(123, gift.getId());
    }

    @Test
    void shouldHandleStrike() {
        var message1 = getGiftMessage("example-new-name", 123, "image-new.png", 1, 1, true);
        var message2 = getGiftMessage("example-new-name", 123, "image-new.png", 2, 1, true);
        var message3 = getGiftMessage("example-new-name", 123, "image-new.png", 0, 1, true);

        var result1 = handler.handleGift(message1);
        var result2 = handler.handleGift(message2);
        var result3 = handler.handleGift(message3);

        var event1 = (TikTokGiftComboEvent) result1.get(0);
        var event2 = (TikTokGiftComboEvent) result2.get(0);

        Assertions.assertEquals(2, result3.size());
        var event3 = (TikTokGiftComboEvent) result3.get(0);

        Assertions.assertEquals(GiftComboStateType.Begin, event1.getComboState());
        Assertions.assertEquals(GiftComboStateType.Active, event2.getComboState());
        Assertions.assertEquals(GiftComboStateType.Finished, event3.getComboState());
    }


    @Test
    void shouldKeepConcurrentStreaksOfSameUserApart() {
        var streakA = getGiftMessage("example-new-name", 123, "image-new.png", 1, 1, true, 111);
        var streakB = getGiftMessage("example-new-name", 123, "image-new.png", 1, 1, true, 222);

        Assertions.assertEquals(GiftComboStateType.Begin, comboStateOf(handler.handleGift(streakA)));
        Assertions.assertEquals(GiftComboStateType.Begin, comboStateOf(handler.handleGift(streakB)));

        var finishA = getGiftMessage("example-new-name", 123, "image-new.png", 0, 1, true, 111);
        var finishB = getGiftMessage("example-new-name", 123, "image-new.png", 0, 1, true, 222);

        //Both streaks must finish on their own, keying on the user alone used to drop one of them
        Assertions.assertEquals(1, countGiftEvents(handler.handleGift(finishA)));
        Assertions.assertEquals(1, countGiftEvents(handler.handleGift(finishB)));
    }

    @Test
    void shouldNotDropStreakOfOtherUserWhenOneFinishes() {
        var userOneActive = getGiftMessage("example-new-name", 123, "image-new.png", 1, 1, true, 111);
        var userTwoActive = getGiftMessage("example-new-name", 123, "image-new.png", 1, 2, true, 222);
        handler.handleGift(userOneActive);
        handler.handleGift(userTwoActive);

        //Finishing user two used to clear() the whole map and reset user one back to Begin
        handler.handleGift(getGiftMessage("example-new-name", 123, "image-new.png", 0, 2, true, 222));

        var next = handler.handleGift(getGiftMessage("example-new-name", 123, "image-new.png", 1, 1, true, 111));
        Assertions.assertEquals(GiftComboStateType.Active, comboStateOf(next));
    }

    @Test
    void shouldIgnoreRepeatedFinishFrame() {
        handler.handleGift(getGiftMessage("example-new-name", 123, "image-new.png", 1, 1, true, 111));

        var first = handler.handleGift(getGiftMessage("example-new-name", 123, "image-new.png", 0, 1, true, 111));
        Assertions.assertEquals(1, countGiftEvents(first));

        //TikTok resends the finishing frame under a fresh msgId; it must not raise onGift again
        clock.addAndGet(150);
        var repeated = handler.handleGift(getGiftMessage("example-new-name", 123, "image-new.png", 0, 1, true, 111));
        Assertions.assertEquals(0, countGiftEvents(repeated));
    }

    @Test
    void shouldNotCutLongRunningStreak() {
        //A streak of 1000 keeps sending frames; the timeout is measured from the last one, so it
        //must never fire mid-streak and split the gift in two
        for (var i = 1; i <= 1000; i++) {
            clock.addAndGet(COMBO_TIMEOUT_MS / 2);
            var frame = handler.handleGift(
                    getGiftMessage("example-new-name", 123, "image-new.png", 1, 1, true, 111));
            Assertions.assertEquals(0, countGiftEvents(frame), "streak was cut at frame " + i);
        }

        //Only the real finishing frame closes it, carrying the full repeat count
        var finish = handler.handleGift(getGiftMessage("example-new-name", 123, "image-new.png", 0, 1, true, 111));
        Assertions.assertEquals(1, countGiftEvents(finish));
    }

    @Test
    void shouldCloseStreakThatNeverReceivedFinishFrame() {
        handler.handleGift(getGiftMessage("example-new-name", 123, "image-new.png", 4, 1, true, 111));

        //Any later gift drives the lazy sweep; the abandoned streak is closed by its last frame
        clock.addAndGet(COMBO_TIMEOUT_MS + 1);
        var unrelated = getGiftMessage("example-new-name", 123, "image-new.png", 0, 9, false, 999);
        var result = handler.handleGift(unrelated);

        //One for the timed out streak, one for the gift that triggered the sweep
        Assertions.assertEquals(2, countGiftEvents(result));
    }

    private GiftComboStateType comboStateOf(List<TikTokEvent> events) {
        return events.stream()
                .filter(TikTokGiftComboEvent.class::isInstance)
                .map(TikTokGiftComboEvent.class::cast)
                .findFirst()
                .orElseThrow()
                .getComboState();
    }

    private long countGiftEvents(List<TikTokEvent> events) {
        return events.stream().filter(e -> !(e instanceof TikTokGiftComboEvent)).count();
    }

    public WebcastGiftMessage getGiftMessage(String giftName,
                                             int giftId,
                                             String giftImage,
                                             int sendType,
                                             int userId,
                                             boolean streakable) {
        return getGiftMessage(giftName, giftId, giftImage, sendType, userId, streakable, 0);
    }

    public WebcastGiftMessage getGiftMessage(String giftName,
                                             int giftId,
                                             String giftImage,
                                             int sendType,
                                             int userId,
                                             boolean streakable,
                                             long groupId) {
        var builder = WebcastGiftMessage.newBuilder();
        var giftBuilder = io.github.jwdeveloper.tiktok.api.messages.data.Gift.newBuilder();
        var userBuilder = User.newBuilder();


        giftBuilder.setId(giftId);
        giftBuilder.setName(giftName);
        giftBuilder.setImage(Image.newBuilder().addUrl(giftImage).build());
        giftBuilder.setType(streakable ? 1 : 0);
        userBuilder.setId(userId);

        builder.setGiftId(giftId);
        builder.setUser(userBuilder);
        builder.setSendType(sendType);
        builder.setGroupId(groupId);
        builder.setGift(giftBuilder);
        return builder.build();
    }


}