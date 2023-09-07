package io.github.jwdeveloper.tiktok.events.objects;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.util.List;

@Value
public class BarrageData {
  String eventType;
  String label;
  List<BarrageUser> users;

  public BarrageData(String eventType, String label, List<BarrageUser> users)
  {
    this.eventType = eventType;
    this.label = label;
    this.users = users;
  }

  @Value
  public static class BarrageUser
  {
      User user;

      String data;
  }
}
