package io.github.jwdeveloper.tiktok.listener;

import io.github.jwdeveloper.tiktok.events.TikTokEventConsumer;

import lombok.Value;

import java.util.List;


@Value
public class ListenerBindingModel
{

    TikTokEventListener listener;

    List<TikTokEventConsumer<?>> events;
}
