package io.github.jwdeveloper.tiktok.events.objects;

import lombok.Getter;
import lombok.Value;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Picture {

  List<String> urls;
  public Picture(io.github.jwdeveloper.tiktok.messages.Image profilePicture) {
    this.urls = profilePicture.getUrlListList();
  }
  public Picture(List<String> urls) {
    this.urls = urls;
  }

  public Picture(String ... urls)
  {
    this.urls = Arrays.stream(urls).toList();
  }


  public static Picture Map(io.github.jwdeveloper.tiktok.messages.Image profilePicture)
  {
    return new Picture(profilePicture.getUrlListList());
  }

  public static Picture Empty()
  {
    return new Picture();
  }

  public static List<Picture> EmptyList()
  {
    return new ArrayList<Picture>();
  }
}
