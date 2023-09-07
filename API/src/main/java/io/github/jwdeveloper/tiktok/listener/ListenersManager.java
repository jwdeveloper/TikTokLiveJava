package io.github.jwdeveloper.tiktok.listener;

import java.util.List;

public interface ListenersManager
{
    List<TikTokEventListener> getBindingModels();
    void addListener(TikTokEventListener listener);

    void removeListener(TikTokEventListener listener);
}
