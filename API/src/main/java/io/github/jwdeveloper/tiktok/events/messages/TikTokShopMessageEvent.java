package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.messages.WebcastOecLiveShoppingMessage;
import lombok.Getter;

@Getter
public class TikTokShopMessageEvent extends TikTokEvent {
  private final String title;

  private final String price;

  private final Picture picture;

  private final String shopUrl;

  private final String shopName;

  public TikTokShopMessageEvent(WebcastOecLiveShoppingMessage msg) {
    super(msg.getHeader());
    var data = msg.getShopData();
    title = data.getTitle();
    price = data.getPriceString();
    picture = new Picture(data.getImageUrl());
    shopUrl = data.getShopUrl();
    shopName = data.getShopName();
  }

}
