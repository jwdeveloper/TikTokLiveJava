package io.github.jwdeveloper.tiktok.events.objects;
import lombok.Value;

@Value
public class TopViewer {
   Integer rank;

   User user;

   Integer coinsGiven;

  public TopViewer(io.github.jwdeveloper.tiktok.messages.TopViewer viewer)
  {
    rank = viewer.getRank();
    user = User.MapOrEmpty(viewer.getUser());
    coinsGiven = viewer.getCoinsGiven();
  }
}
