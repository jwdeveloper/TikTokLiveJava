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


/**
 *
 * @see ListenersManager
 *
 * TikTokEventListener is an alternative way of handing TikTok events.
 *
 *  TikTokLive.newClient("someuser").addListener(listener)
 *
 *  After registertion all listeners are kept in Listener manager
 *  that could be obtained by client.getListenerManager();
 *
 *  Method in TikTokEventListener should meet 4 requirements to be detected
 *         - must have @TikTokEventHandler annotation
 *         - must have 2 parameters
 *         - first parameter must be LiveClient
 *         - second must be class that extending TikTokEvent
 *
 *  public static class CustomListener implements TikTokEventListener
 *     {
 *         @TikTokEventHandler
 *         public void onError(LiveClient liveClient, TikTokErrorEvent event)
 *         {
 *             System.out.println(event.getException().getMessage());
 *         }
 *
 *         @TikTokEventHandler
 *         public void onCommentMessage(LiveClient liveClient, TikTokCommentEvent event)
 *         {
 *             System.out.println(event.getText());
 *         }
 *
 *         @TikTokEventHandler
 *         public void onGiftMessage(LiveClient liveClient, TikTokGiftMessageEvent event)
 *         {
 *             System.out.println(event.getGift().getDescription());
 *         }
 *
 *         @TikTokEventHandler
 *         public void onAnyEvent(LiveClient liveClient, TikTokEvent event)
 *         {
 *             System.out.println(event.getClass().getSimpleName());
 *         }
 *     }
 *
 */
public interface TikTokEventListener
{

}
