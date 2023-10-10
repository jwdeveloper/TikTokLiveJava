
<div align="center" >


<a target="blank" >

<img src="https://raw.githubusercontent.com/jwdeveloper/TikTokLiveJava/develop-1_0_0/Tools-ReadmeGenerator/src/main/resources/logo.svg" width="15%" >
</img>
</a>

</div>
<div align="center" >

<h1>TikTokLive Java</h1>


 

❤️❤️🎁 *Connect to TikTok live in 3 lines* 🎁❤️❤️
  



<div align="center" >


<a href="https://jitpack.io/#jwdeveloper/DepenDance" target="blank" >

<img src="https://jitpack.io/v/jwdeveloper/DepenDance.svg" width="20%" >
</img>
</a>

<a href="https://discord.gg/2hu6fPPeF7" target="blank" >

<img src="https://img.shields.io/badge/Discord-%235865F2.svg?style=for-the-badge&logo=discord&logoColor=white" >
</img>
</a>



<a target="blank" >

<img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" >
</img>
</a>

</div>

</div>







# Introdution
A Java library based on [TikTokLive](https://github.com/isaackogan/TikTokLive) and [TikTokLiveSharp](https://github.com/sebheron/TikTokLiveSharp). Use it to receive live stream events such as comments and gifts in realtime from [TikTok LIVE](https://www.tiktok.com/live) by connecting to TikTok's internal WebCast push service. The package includes a wrapper that connects to the WebCast service using just the username (`uniqueId`). This allows you to connect to your own live chat as well as the live chat of other streamers. No credentials are required. Besides [Chat Comments](#chat), other events such as [Members Joining](#member), [Gifts](#gift), [Subscriptions](#subscribe), [Viewers](#roomuser), [Follows](#social), [Shares](#social), [Questions](#questionnew), [Likes](#like) and [Battles](#linkmicbattle) can be tracked. You can also send [automatic messages](#send-chat-messages) into the chat by providing your Session ID.

Join the support [discord](https://discord.gg/e2XwPNTBBr) and visit the `#java-support` channel for questions, contributions and ideas. Feel free to make pull requests with missing/new features, fixes, etc

Do you prefer other programming languages?
- **Node** orginal: [TikTok-Live-Connector](https://github.com/isaackogan/TikTok-Live-Connector) by [@zerodytrash](https://github.com/zerodytrash)
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
            <version>NOT_FOUND</version>
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

   TikTokLive.newClient(SimpleExample.TIKTOK_HOSTNAME)
                .onGift((liveClient, event) ->
                {
                    switch (event.getGift()) {
                        case ROSE -> print(ConsoleColors.RED, "Rose!");
                        case GG -> print(ConsoleColors.YELLOW, " GOOD GAME!");
                        case TIKTOK -> print(ConsoleColors.CYAN,"Thanks for TikTok");
                        default -> print(ConsoleColors.GREEN, "[Thanks for gift] ", ConsoleColors.YELLOW, event.getGift().getName(), "x", event.getCombo());
                    }
                })
                .onConnected((client, event) ->
                {
                    print(ConsoleColors.GREEN, "[Connected]");
                })
                .onJoin((client, event) ->
                {
                    print(ConsoleColors.WHITE, "Join:", ConsoleColors.WHITE_BRIGHT, event.getUser().getName());
                })
                .onComment((client, event) ->
                {
                    print(ConsoleColors.GREEN, event.getUser().getName(), ":", ConsoleColors.WHITE_BRIGHT, event.getText());
                })
                .onError((client, event) ->
                {
                    event.getException().printStackTrace();
                })
                .buildAndConnectAsync();

```
## Events



**Control**:

- [onReconnecting](#onreconnecting-tiktokreconnectingevent)
- [onDisconnected](#ondisconnected-tiktokdisconnectedevent)
- [onConnected](#onconnected-tiktokconnectedevent)
- [onError](#onerror-tiktokerrorevent)

**Message**:

- [onEvent](#onevent-tiktokevent)
- [onLiveEnded](#onliveended-tiktokliveendedevent)
- [onShare](#onshare-tiktokshareevent)
- [onEmote](#onemote-tiktokemoteevent)
- [onComment](#oncomment-tiktokcommentevent)
- [onLike](#onlike-tiktoklikeevent)
- [onRoom](#onroom-tiktokroomevent)
- [onJoin](#onjoin-tiktokjoinevent)
- [onUnhandledSocial](#onunhandledsocial-tiktokunhandledsocialevent)
- [onRoomUserInfo](#onroomuserinfo-tiktokroomuserinfoevent)
- [onQuestion](#onquestion-tiktokquestionevent)
- [onSubscribe](#onsubscribe-tiktoksubscribeevent)
- [onLivePaused](#onlivepaused-tiktoklivepausedevent)
- [onGiftCombo](#ongiftcombo-tiktokgiftcomboevent)
- [onGift](#ongift-tiktokgiftevent)
- [onFollow](#onfollow-tiktokfollowevent)

**Debug**:

- [onWebsocketUnhandledMessage](#onwebsocketunhandledmessage-tiktokwebsocketunhandledmessageevent)
- [onWebsocketResponse](#onwebsocketresponse-tiktokwebsocketresponseevent)
- [onWebsocketMessage](#onwebsocketmessage-tiktokwebsocketmessageevent)
# Examples
<br>


#### onReconnecting [TikTokReconnectingEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)



```java
TikTokLive.newClient("host-name")
.onReconnecting((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onDisconnected [TikTokDisconnectedEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggered when the connection gets disconnected. In that case you can call connect() again to have a reconnect logic.
Note that you should wait a little bit before attempting a reconnect to to avoid being rate-limited.


```java
TikTokLive.newClient("host-name")
.onDisconnected((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onConnected [TikTokConnectedEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggered when the connection is successfully established.


```java
TikTokLive.newClient("host-name")
.onConnected((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onError [TikTokErrorEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


General error event. You should handle this.


```java
TikTokLive.newClient("host-name")
.onError((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onEvent [TikTokEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Base class for all events


```java
TikTokLive.newClient("host-name")
.onEvent((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onLiveEnded [TikTokLiveEndedEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggered when the live stream gets terminated by the host. Will also trigger the TikTokDisconnectedEvent event.


```java
TikTokLive.newClient("host-name")
.onLiveEnded((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onShare [TikTokShareEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggers when a user shares the stream. Based on social event.


```java
TikTokLive.newClient("host-name")
.onShare((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onEmote [TikTokEmoteEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggered every time a subscriber sends an emote (sticker).


```java
TikTokLive.newClient("host-name")
.onEmote((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onComment [TikTokCommentEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggered every time a new chat comment arrives.


```java
TikTokLive.newClient("host-name")
.onComment((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onLike [TikTokLikeEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggered when a viewer sends likes to the streamer. For streams with many viewers, this event is not always triggered by TikTok.


```java
TikTokLive.newClient("host-name")
.onLike((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onRoom [TikTokRoomEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)



```java
TikTokLive.newClient("host-name")
.onRoom((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onJoin [TikTokJoinEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)



```java
TikTokLive.newClient("host-name")
.onJoin((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onUnhandledSocial [TikTokUnhandledSocialEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)



```java
TikTokLive.newClient("host-name")
.onUnhandledSocial((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onRoomUserInfo [TikTokRoomUserInfoEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


      Only top 5 users in ranking has detailed data
      rest has only ID


```java
TikTokLive.newClient("host-name")
.onRoomUserInfo((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onQuestion [TikTokQuestionEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggered every time someone asks a new question via the question feature.


```java
TikTokLive.newClient("host-name")
.onQuestion((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onSubscribe [TikTokSubscribeEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggers when a user creates a subscription.


```java
TikTokLive.newClient("host-name")
.onSubscribe((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onLivePaused [TikTokLivePausedEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)



```java
TikTokLive.newClient("host-name")
.onLivePaused((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onGiftCombo [TikTokGiftComboEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)



```java
TikTokLive.newClient("host-name")
.onGiftCombo((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onGift [TikTokGiftEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggered every time a gift arrives.


```java
TikTokLive.newClient("host-name")
.onGift((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onFollow [TikTokFollowEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggers when a user follows the streamer. Based on social event.


```java
TikTokLive.newClient("host-name")
.onFollow((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onWebsocketUnhandledMessage [TikTokWebsocketUnhandledMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggered every time a protobuf encoded webcast message arrives. You can deserialize the binary object depending on the use case.


```java
TikTokLive.newClient("host-name")
.onWebsocketUnhandledMessage((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onWebsocketResponse [TikTokWebsocketResponseEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)



```java
TikTokLive.newClient("host-name")
.onWebsocketResponse((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>


#### onWebsocketMessage [TikTokWebsocketMessageEvent](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages.java)


Triggered every time a protobuf encoded webcast message arrives. You can deserialize the binary object depending on the use case.


```java
TikTokLive.newClient("host-name")
.onWebsocketMessage((liveClient, event) ->
{

})
.buildAndConnect();
```





<br>
<br>

## Listener Example

```java
{{listener-content}}
```

## Contributing
Your improvements are welcome! Feel free to open an <a href="https://github.com/jwdeveloper/TikTok-Live-Java/issues">issue</a> or <a href="https://github.com/jwdeveloper/TikTok-Live-Java/pulls">pull request</a>.
