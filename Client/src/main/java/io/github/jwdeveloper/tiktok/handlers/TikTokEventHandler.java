package io.github.jwdeveloper.tiktok.handlers;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class TikTokEventHandler  {
    private final Map<String, Consumer> events;

    public TikTokEventHandler()
    {
        events = new HashMap<>();
    }

    public void publish(TikTokEvent tikTokEvent)
    {
        if(events.containsKey(TikTokEvent.class.getSimpleName()))
        {
            var handler = events.get(TikTokEvent.class.getSimpleName());
            handler.accept(tikTokEvent);
        }

        var name = tikTokEvent.getClass().getSimpleName();
        if(!events.containsKey(name))
        {
            return;
        }
        var handler = events.get(name);
        handler.accept(tikTokEvent);
    }

    public <T extends TikTokEvent> void subscribe(Class<?> clazz, Consumer<T> event)
    {
        events.put(clazz.getSimpleName(),event);
    }



}
