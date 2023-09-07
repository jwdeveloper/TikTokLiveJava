package io.github.jwdeveloper.tiktok.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface EventMeta
{
     EventType eventType();
}
