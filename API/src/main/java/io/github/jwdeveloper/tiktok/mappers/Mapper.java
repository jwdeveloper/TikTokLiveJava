package io.github.jwdeveloper.tiktok.mappers;

public interface Mapper<SOURCE,TARGET>
{
     TARGET mapFrom(SOURCE source);
}
