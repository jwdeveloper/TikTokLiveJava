package io.github.jwdeveloper.tiktok.events.objects;

import lombok.Getter;

@Getter
public class TopViewer {
  private Integer rank;

  private User user;

  private Integer coinsGiven;

  public TopViewer(io.github.jwdeveloper.tiktok.messages.TopViewer viewer)
  {
    rank = viewer.getRank();
    if(viewer.hasUser())
    {
      user = new User(viewer.getUser());
    }
    coinsGiven = viewer.getCoinsGiven();
  }
}
