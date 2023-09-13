package io.github.jwdeveloper.tiktok.listener;

import java.util.List;

/**
 *  You can dynamically add or removing TikTokEventListener
 *
 * @see TikTokEventListener
 *
 */
public interface ListenersManager
{
    List<TikTokEventListener> getListeners();
    void addListener(TikTokEventListener listener);

    void removeListener(TikTokEventListener listener);
}
