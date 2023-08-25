[![](https://jitpack.io/v/jwdeveloper/TikTok-Live-Java.svg)](https://jitpack.io/#jwdeveloper/TikTok-Live-Java)


# TikTok-Live-Java
A Java library based on [TikTok-Connector](https://github.com/isaackogan/TikTok-Live-Connector) and [TikTokLiveSharp](https://github.com/sebheron/TikTokLiveSharp). Use it to receive live stream events such as comments and gifts in realtime from [TikTok LIVE](https://www.tiktok.com/live) by connecting to TikTok's internal WebCast push service. The package includes a wrapper that connects to the WebCast service using just the username (`uniqueId`). This allows you to connect to your own live chat as well as the live chat of other streamers. No credentials are required. Besides [Chat Comments](#chat), other events such as [Members Joining](#member), [Gifts](#gift), [Subscriptions](#subscribe), [Viewers](#roomuser), [Follows](#social), [Shares](#social), [Questions](#questionnew), [Likes](#like) and [Battles](#linkmicbattle) can be tracked. You can also send [automatic messages](#send-chat-messages) into the chat by providing your Session ID.


Do you prefer other programming languages?
- **Node** orginal: [TikTok-Live-Connector](https://github.com/isaackogan/TikTok-Live-Connector) by [@isaackogan](https://github.com/zerodytrash) 
- **Python** rewrite: [TikTokLive](https://github.com/isaackogan/TikTokLive) by [@isaackogan](https://github.com/isaackogan)
- **Go** rewrite: [GoTikTokLive](https://github.com/Davincible/gotiktoklive) by [@Davincible](https://github.com/Davincible)
- **C#** rewrite: [TikTokLiveSharp](https://github.com/frankvHoof93/TikTokLiveSharp) by [@frankvHoof93](https://github.com/frankvHoof93)

**NOTE:** This is not an official API. It's a reverse engineering project.

#### Overview
- [Getting started](#getting-started)
- [Params and options](#params-and-options)
- [Methods](#methods)
- [Events](#events)
- [Examples](#examples)
- [Contributing](#contributing)

## Getting started

1. Install the package via Maven 

```xml
   <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>
```

```xml
   <dependencies>
         <dependency>
            <groupId>com.github.jwdeveloper.TikTok-Live-Java</groupId>
            <artifactId>Client</artifactId>
            <version>0.0.14-Release</version>
            <scope>compile</scope>
        </dependency>
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
   </dependencies>
```

2. Create your first chat connection

```java
  public static void main(String[] args)
  {
     // Username of someone who is currently live
     var tiktokUsername = "jwdevtiktok";

     TikTokLive.newClient(tiktokUsername)
                .onConnected((client, event) ->
                {
                    System.out.println("Connected");
                })
                .onJoin((client, event)  ->
                {
                    System.out.println("User joined -> " + event.getUser().getNickName());
                })
                .onComment((client, event)  ->
                {
                   System.out.println(event.getUser().getUniqueId() + ": " + event.getText());
                })
                .onEvent((client, event) ->
                {
                    System.out.println("Viewers count: "+client.getRoomInfo().getViewersCount());
                })
                .onError((client, event)  ->
                {
                    event.getException().printStackTrace();
                })
                .buildAndRun();
        System.in.read();
    }
```
## Configuration

```java
public class ConfigurationExample
{
    public static void main(String[] args) throws IOException {
       var tiktokUsername = "jwdevtiktok";
       TikTokLive.newClient(tiktokUsername)
                .configure(clientSettings ->
                {
                    clientSettings.setHostName(Main.TEST_TIKTOK_USER); // TikTok user name
                    clientSettings.setClientLanguage("en"); // Language
                    clientSettings.setTimeout(Duration.ofSeconds(2)); // Connection timeout
                    clientSettings.setLogLevel(Level.ALL); // Log level
                    clientSettings.setDownloadGiftInfo(true); // Downloading meta information about gifts. You can access it by client.getGiftManager().getGiftsInfo();
                    clientSettings.setPrintMessageData(true); // Printing TikTok Protocol buffer messages in Base64 format
                    clientSettings.setPrintToConsole(true); // Printing all logs to console even if log level is Level.OFF
                    clientSettings.setHandleExistingMessagesOnConnect(true); // Invokes all TikTok events that had occurred before connection
                    clientSettings.setRetryOnConnectionFailure(true); // Reconnecting if TikTok user is offline
                    clientSettings.setRetryConnectionTimeout(Duration.ofSeconds(1)); // Timeout before next reconnection
                })
                .buildAndRun();
       System.in.read();
    }
}

```

## Methods
A `client (LiveClient)` object contains the following methods.

| Method Name | Description |
| ----------- | ----------- |
| connect     | Connects to the live stream chat.<br>Returns a `Promise` which will be resolved when the connection is successfully established. |
| disconnect  | Disconnects the connection. |
| getGiftManager  |  Gets the meta informations about all gifts. |
| getRoomInfo | Gets the current room info from TikTok API including streamer info, room status and statistics. |

## Events

A `TikTokLive` object has the following events 

Events:
-  [TikTokUnhandledSocialEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokUnhandledSocialEvent.java)
-  [TikTokLinkMicFanTicketEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLinkMicFanTicketEvent.java)
-  [TikTokEnvelopeEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokEnvelopeEvent.java)
-  [TikTokShopMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokShopMessageEvent.java)
-  [TikTokDetectMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokDetectMessageEvent.java)
-  [TikTokLinkLayerMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLinkLayerMessageEvent.java)
-  [TikTokConnectedEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokConnectedEvent.java)
-  [TikTokCaptionEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokCaptionEvent.java)
-  [TikTokQuestionEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokQuestionEvent.java)
-  [TikTokRoomPinMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokRoomPinMessageEvent.java)
-  [TikTokRoomMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokRoomMessageEvent.java)
-  [TikTokLivePausedEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLivePausedEvent.java)
-  [TikTokLikeEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLikeEvent.java)
-  [TikTokLinkMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLinkMessageEvent.java)
-  [TikTokBarrageMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokBarrageMessageEvent.java)
-  [TikTokGiftMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokGiftMessageEvent.java)
-  [TikTokLinkMicArmiesEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLinkMicArmiesEvent.java)
-  [TikTokEmoteEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokEmoteEvent.java)
-  [TikTokUnauthorizedMemberEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokUnauthorizedMemberEvent.java)
-  [TikTokInRoomBannerEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokInRoomBannerEvent.java)
-  [TikTokLinkMicMethodEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLinkMicMethodEvent.java)
-  [TikTokSubscribeEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokSubscribeEvent.java)
-  [TikTokPollMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokPollMessageEvent.java)
-  [TikTokFollowEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokFollowEvent.java)
-  [TikTokRoomViewerDataEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokRoomViewerDataEvent.java)
-  [TikTokGoalUpdateEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokGoalUpdateEvent.java)
-  [TikTokCommentEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokCommentEvent.java)
-  [TikTokRankUpdateEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokRankUpdateEvent.java)
-  [TikTokIMDeleteEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokIMDeleteEvent.java)
-  [TikTokLiveEndedEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLiveEndedEvent.java)
-  [TikTokErrorEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokErrorEvent.java)
-  [TikTokUnhandledEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokUnhandledEvent.java)
-  [TikTokJoinEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokJoinEvent.java)
-  [TikTokRankTextEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokRankTextEvent.java)
-  [TikTokShareEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokShareEvent.java)
-  [TikTokUnhandledMemberEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokUnhandledMemberEvent.java)
-  [TikTokSubNotifyEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokSubNotifyEvent.java)
-  [TikTokLinkMicBattleEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLinkMicBattleEvent.java)
-  [TikTokDisconnectedEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokDisconnectedEvent.java)
-  [TikTokGiftBroadcastEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokGiftBroadcastEvent.java)
-  [TikTokUnhandledControlEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokUnhandledControlEvent.java)
-  [TikTokEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokEvent.java)


<br><br>

## Contributing
Your improvements are welcome! Feel free to open an <a href="https://github.com/jwdeveloper/TikTok-Live-Java/issues">issue</a> or <a href="https://github.com/jwdeveloper/TikTok-Live-Java/pulls">pull request</a>.
