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
