[![](https://jitpack.io/v/jwdeveloper/TikTok-Live-Java.svg)](https://jitpack.io/#jwdeveloper/TikTok-Live-Java)


# TikTokLive Java
A Java library based on [TikTokLive](https://github.com/isaackogan/TikTokLive) and [TikTokLiveSharp](https://github.com/sebheron/TikTokLiveSharp). Use it to receive live stream events such as comments and gifts in realtime from [TikTok LIVE](https://www.tiktok.com/live) by connecting to TikTok's internal WebCast push service. The package includes a wrapper that connects to the WebCast service using just the username (`uniqueId`). This allows you to connect to your own live chat as well as the live chat of other streamers. No credentials are required. Besides [Chat Comments](#chat), other events such as [Members Joining](#member), [Gifts](#gift), [Subscriptions](#subscribe), [Viewers](#roomuser), [Follows](#social), [Shares](#social), [Questions](#questionnew), [Likes](#like) and [Battles](#linkmicbattle) can be tracked. You can also send [automatic messages](#send-chat-messages) into the chat by providing your Session ID.

Join the support [discord](https://discord.gg/e2XwPNTBBr) and visit the `#java-support` channel for questions, contributions and ideas. Feel free to make pull requests with missing/new features, fixes, etc

Do you prefer other programming languages?
- **Node** orginal: [TikTok-Live-Connector](https://github.com/zerodytrash/TikTok-Live-Connector) by [@zerodytrash](https://github.com/zerodytrash)
- **Python** rewrite: [TikTokLive](https://github.com/isaackogan/TikTokLive) by [@isaackogan](https://github.com/isaackogan)
- **Go** rewrite: [GoTikTokLive](https://github.com/Davincible/gotiktoklive) by [@Davincible](https://github.com/Davincible)
- **C#** rewrite: [TikTokLiveSharp](https://github.com/frankvHoof93/TikTokLiveSharp) by [@frankvHoof93](https://github.com/frankvHoof93)

**NOTE:** This is not an official API. It's a reverse engineering project.

#### Overview
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [Methods](#methods)
- [Events](#events)
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

   <dependencies>
         <dependency>
            <groupId>com.github.jwdeveloper.TikTok-Live-Java</groupId>
            <artifactId>Client</artifactId>
            <version>0.0.25-Release</version>
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
package io.github.jwdeveloper.tiktok;
import java.io.IOException;

public class SimpleExample {
    public static void main(String[] args) throws IOException {

        // set tiktok username
        TikTokLive.newClient(Main.TEST_TIKTOK_USER)
                .configure(clientSettings ->
                {

                })
                .onFollow((liveClient, event) ->
                {
                    System.out.println("Follow joined -> " + event.getNewFollower().getNickName());
                })
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
}

```
## Configuration

```java
package io.github.jwdeveloper.tiktok;

import java.io.IOException;
import java.time.Duration;
import java.util.logging.Level;

public class ConfigurationExample {
    public static void main(String[] args) throws IOException {

        TikTokLive.newClient(Main.TEST_TIKTOK_USER)
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

                    //Optional: Sometimes not every message from chat are send to TikTokLiveJava to fix this issue you can set sessionId
                    // documentation how to obtain sessionId https://github.com/isaackogan/TikTok-Live-Connector#send-chat-messages
                    clientSettings.setSessionId("86c3c8bf4b17ebb2d74bb7fa66fd0000");

                    //Optional:
                    clientSettings.setRoomId("XXXXXXXXXXXXXXXXX");
                })
                .buildAndRun();
        System.in.read();
    }
}

```
## Listener Example

```java
package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.annotations.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokGiftMessageEvent;
import io.github.jwdeveloper.tiktok.events.messages.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.listener.TikTokEventListener;
import io.github.jwdeveloper.tiktok.live.LiveClient;

import java.io.IOException;

public class ListenerExample
{
    public static void main(String[] args) throws IOException {

        CustomListener customListener = new CustomListener();

        // set tiktok username
        var client = TikTokLive.newClient(Main.TEST_TIKTOK_USER)
                .addListener(customListener)
                .buildAndRun();

        System.in.read();
    }

    /*
       Method in TikTokEventListener should meet 4 requirements to be detected
        - must have @TikTokEventHandler annotation
        - must have 2 parameters
        - first parameter must be LiveClient
        - second must be class that extending TikTokEvent
     */
    public static class CustomListener implements TikTokEventListener
    {

        @TikTokEventHandler
        public void onLike(LiveClient liveClient, TikTokLikeEvent event)
        {
            System.out.println(event.toString());
        }

        @TikTokEventHandler
        public void onError(LiveClient liveClient, TikTokErrorEvent event)
        {
            System.out.println(event.getException().getMessage());
        }

        @TikTokEventHandler
        public void onCommentMessage(LiveClient liveClient, TikTokCommentEvent event)
        {
            System.out.println(event.getText());
        }

        @TikTokEventHandler
        public void onGiftMessage(LiveClient liveClient, TikTokGiftMessageEvent event)
        {
            System.out.println(event.getGift().getDescription());
        }

        @TikTokEventHandler
        public void onAnyEvent(LiveClient liveClient, TikTokEvent event)
        {
            System.out.println(event.getClass().getSimpleName());
        }

    }
}

```

## Methods
A `client (LiveClient)` object contains the following methods.



| Method Name         | Description |
|---------------------| ----------- |
| connect             | Connects to the live stream. |
| disconnect          | Disconnects the connection. |
| getGiftManager      |  Gets the meta informations about all gifts. |
| getRoomInfo         | Gets the current room info from TikTok API including streamer info, room status and statistics. |
| getListenersManager | Gets and manage TikTokEventListeners    |
## Events

A `TikTokLive` object has the following events


**Message**:
-  [TikTokLinkMicFanTicketEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLinkMicFanTicketEvent.java)
-  [TikTokEnvelopeEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokEnvelopeEvent.java)
-  [TikTokShopMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokShopMessageEvent.java)
-  [TikTokDetectMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokDetectMessageEvent.java)
-  [TikTokLinkLayerMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLinkLayerMessageEvent.java)
-  [TikTokCaptionEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokCaptionEvent.java)
-  [TikTokQuestionEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokQuestionEvent.java)
-  [TikTokRoomPinMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokRoomPinMessageEvent.java)
-  [TikTokRoomMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokRoomMessageEvent.java)
-  [TikTokLinkMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLinkMessageEvent.java)
-  [TikTokBarrageMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokBarrageMessageEvent.java)
-  [TikTokGiftMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokGiftMessageEvent.java)
-  [TikTokLinkMicArmiesEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLinkMicArmiesEvent.java)
-  [TikTokEmoteEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokEmoteEvent.java)
-  [TikTokUnauthorizedMemberEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokUnauthorizedMemberEvent.java)
-  [TikTokInRoomBannerEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokInRoomBannerEvent.java)
-  [TikTokLinkMicMethodEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLinkMicMethodEvent.java)
-  [TikTokPollMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokPollMessageEvent.java)
-  [TikTokRoomViewerDataEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokRoomViewerDataEvent.java)
-  [TikTokGoalUpdateEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokGoalUpdateEvent.java)
-  [TikTokCommentEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokCommentEvent.java)
-  [TikTokRankUpdateEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokRankUpdateEvent.java)
-  [TikTokIMDeleteEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokIMDeleteEvent.java)
-  [TikTokRankTextEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokRankTextEvent.java)
-  [TikTokUnhandledMemberEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokUnhandledMemberEvent.java)
-  [TikTokSubNotifyEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokSubNotifyEvent.java)
-  [TikTokLinkMicBattleEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLinkMicBattleEvent.java)
-  [TikTokGiftBroadcastEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokGiftBroadcastEvent.java)
-  [TikTokUnhandledWebsocketMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokUnhandledWebsocketMessageEvent.java)

**Control**:
-  [TikTokConnectedEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokConnectedEvent.java)
-  [TikTokReconnectingEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokReconnectingEvent.java)
-  [TikTokErrorEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokErrorEvent.java)
-  [TikTokDisconnectedEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokDisconnectedEvent.java)

**Custom**:
-  [TikTokHeaderEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokHeaderEvent.java)
-  [TikTokUnhandledSocialEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokUnhandledSocialEvent.java)
-  [TikTokLivePausedEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLivePausedEvent.java)
-  [TikTokLikeEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLikeEvent.java)
-  [TikTokWebsocketMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokWebsocketMessageEvent.java)
-  [TikTokSubscribeEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokSubscribeEvent.java)
-  [TikTokFollowEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokFollowEvent.java)
-  [TikTokLiveClientEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLiveClientEvent.java)
-  [TikTokUnhandledEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokUnhandledEvent.java)
-  [TikTokLiveEndedEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokLiveEndedEvent.java)
-  [TikTokJoinEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokJoinEvent.java)
-  [TikTokShareEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokShareEvent.java)
-  [TikTokUnhandledControlEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokUnhandledControlEvent.java)


<br><br>

## Contributing
Your improvements are welcome! Feel free to open an <a href="https://github.com/jwdeveloper/TikTok-Live-Java/issues">issue</a> or <a href="https://github.com/jwdeveloper/TikTok-Live-Java/pulls">pull request</a>.
