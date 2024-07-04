/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.listener;

import io.github.jwdeveloper.tiktok.live.LiveClient;

/**
 * ListenersManager
 * <p>
 * TikTokEventListener is an alternative way of handing TikTok events.
 * <p>
 * {@code TikTokLive.newClient("someuser").addListener(listener);}
 * <p>
 * After registertion, all listeners are kept in Listener manager - {@link  LiveClient#getListenersManager()}
 * <p>
 * Method in TikTokEventListener should meet requirements below to be detected
 * <p>- @TikTokEventObserver annotation
 * <p>- 2 parameters of (LiveClient, Class extending TikTokEvent)
 * <pre>
 * {@code
 * 	public static class CustomListener implements TikTokEventListener
 *  {
 *    @TikTokEventObserver
 *      public void onError(LiveClient liveClient, TikTokErrorEvent event)
 *      {
 *      	System.out.println(event.getException().getMessage());
 *      }
 *
 *      @TikTokEventObserver
 *      public void onCommentMessage(LiveClient liveClient, TikTokCommentEvent event)
 *      {
 *          System.out.println(event.getText());
 *      }
 *
 *      @TikTokEventObserver
 *      public void onGiftMessage(LiveClient liveClient, TikTokGiftMessageEvent event)
 *      {
 *          System.out.println(event.getGift().getDescription());
 *      }
 *
 *      @TikTokEventObserver
 *      public void onAnyEvent(LiveClient liveClient, TikTokEvent event)
 *      {
 *          System.out.println(event.getClass().getSimpleName());
 *      }
 *   }
 *  }
 *  </pre>
 */
//TODO I think this interface can be removed, since we are using,
//annotation @TikTokEventHandler to check methods that are events
@Deprecated(forRemoval = true, since = "1.8.1 (This interface is not longer needed, please remove it from your class)")
public interface TikTokEventListener {

}