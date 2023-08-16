package io.github.jwdeveloper.tiktok.events.objects;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public class Picture {

  public final List<String> urls;

  public Picture(io.github.jwdeveloper.tiktok.messages.Picture profilePicture) {
    this.urls = profilePicture.getUrlsList();
  }

  public Picture(List<String> urls) {
    this.urls = urls;
  }

  public Picture(String ... urls)
  {
    this.urls = Arrays.stream(urls).toList();
  }
}
