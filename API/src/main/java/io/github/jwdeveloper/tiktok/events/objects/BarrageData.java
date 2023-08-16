package io.github.jwdeveloper.tiktok.events.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
public class BarrageData {
  private final String eventType;
  private final String label;
  private final List<BarrageUser> users;

  public BarrageData(String eventType, String label, List<BarrageUser> users)
  {
    this.eventType = eventType;
    this.label = label;
    this.users = users;
  }

  @Getter
  @AllArgsConstructor
  public static final class BarrageUser
  {
      private final User user;

      private final String data;
  }
}
