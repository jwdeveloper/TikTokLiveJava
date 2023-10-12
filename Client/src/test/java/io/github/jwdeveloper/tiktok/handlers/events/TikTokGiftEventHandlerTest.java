package io.github.jwdeveloper.tiktok.handlers.events;

import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.models.Picture;
import io.github.jwdeveloper.tiktok.gifts.TikTokGiftManager;
import io.github.jwdeveloper.tiktok.messages.data.GiftStruct;
import io.github.jwdeveloper.tiktok.messages.data.Image;
import io.github.jwdeveloper.tiktok.messages.data.User;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastGiftMessage;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;


@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TikTokGiftEventHandlerTest {

    public static TikTokGiftEventHandler handler;


    @BeforeAll
    public void before() {
        var manager = new TikTokGiftManager();
        manager.registerGift(123, "example", 123, new Picture("image.webp"));
        handler = new TikTokGiftEventHandler(manager);
    }

    @Test
    void shouldHandleGifts() {
        var message = getGiftMessage("example-new-name", 123, "image-new.png", 0, 1);
        var result = handler.handleGift(message);

        Assertions.assertEquals(1, result.size());

        var event = (TikTokGiftEvent) result.get(0);
        var gift = event.getGift();
        Assertions.assertEquals("image-new.png",gift.getPicture().getLink());
        Assertions.assertEquals(123,gift.getId());
    }


    public WebcastGiftMessage getGiftMessage(String giftName,
                                             int giftId,
                                             String giftImage,
                                             int sendType,
                                             int userId) {
        var builder = WebcastGiftMessage.newBuilder();
        var giftBuilder = GiftStruct.newBuilder();
        var userBuilder = User.newBuilder();


        giftBuilder.setId(giftId);
        giftBuilder.setName(giftName);
        giftBuilder.setImage(Image.newBuilder().addUrlList(giftImage).build());
        userBuilder.setId(userId);

        builder.setGiftId(giftId);
        builder.setUser(userBuilder);
        builder.setSendType(sendType);
        builder.setGift(giftBuilder);
        return builder.build();
    }

}