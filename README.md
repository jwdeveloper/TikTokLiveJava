<div align="center">
<a target="blank">
<img src="https://raw.githubusercontent.com/jwdeveloper/TikTokLiveJava/develop-1_0_0/Tools-ReadmeGenerator/src/main/resources/logo.svg" width="15%" >
</a>
</div>
<div align="center" >
<h1>TikTok LIVE API for Java</h1>

❤️❤️🎁 *Connect to TikTok LIVE in 3 lines* 🎁❤️❤️

<div align="center" >
<a href="https://jitpack.io/#jwdeveloper/TikTok-Live-Java" target="blank" >
<img src="https://jitpack.io/v/jwdeveloper/TikTok-Live-Java.svg" width="20%" >
</a>


<a href="https://discord.gg/e2XwPNTBBr" target="blank" >
<img src="https://img.shields.io/badge/Discord-%235865F2.svg?style=for-the-badge&logo=discord&logoColor=white" >
</a>

<a target="blank" >
<img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white" >
</a>
</div>
</div>

# Introduction
A Java library inspired by [TikTokLive](https://github.com/isaackogan/TikTokLive) and [TikTokLiveSharp](https://github.com/frankvHoof93/TikTokLiveSharp). Use it to receive live stream events such as comments and gifts in realtime from [TikTok LIVE](https://www.tiktok.com/live) by connecting to TikTok's internal WebCast push service.
The library includes a wrapper that connects to the WebCast service using just the username (`uniqueId`). This allows you to connect to your own live chat as well as the live chat of other streamers.
No credentials are required. Events such as [Members Joining](#member), [Gifts](#gift), [Subscriptions](#subscribe), [Viewers](#roomuser), [Follows](#social), [Shares](#social), [Questions](#questionnew), [Likes](#like) and [Battles](#linkmicbattle) can be tracked.

Full documentation, guides and the complete event reference: [ttlj.docs.isaackogan.com](https://ttlj.docs.isaackogan.com/)


<div align="center">
  <a href="https://www.youtube.com/watch?v=eerWGgUKc6c" align="right" target="blank"><img src="https://img.youtube.com/vi/eerWGgUKc6c/hqdefault.jpg" alt="IMAGE ALT TEXT" width="38%" align="right"></a>
</div>

Join the support [discord](https://discord.gg/e2XwPNTBBr) and visit the `#java-support` channel for questions, contributions and ideas. Feel free to make pull requests with missing/new features, fixes, etc

Do you prefer other programming languages?
- **Node** orginal: [TikTok-Live-Connector](https://github.com/zerodytrash/TikTok-Live-Connector) by [@zerodytrash](https://github.com/zerodytrash)
- **Rust** rewrite: [TikTokLiveRust](https://github.com/jwdeveloper/TikTokLiveRust)
- **Python** rewrite: [TikTokLive](https://github.com/isaackogan/TikTokLive) by [@isaackogan](https://github.com/isaackogan)
- **Go** rewrite: [GoTikTokLive](https://github.com/Davincible/gotiktoklive) by [@Davincible](https://github.com/Davincible)
- **C#** rewrite: [TikTokLiveSharp](https://github.com/frankvHoof93/TikTokLiveSharp) by [@frankvHoof93](https://github.com/frankvHoof93)

See [Euler Stream](https://www.eulerstream.com/docs/api/quickstart) for quickstart instructions in multiple languages.

**NOTE:** This is not an official API. It's a reverse engineering project.

#### Overview
- [Getting started](#getting-started)
- [Events](#events)
- [Extensions](#extensions)
- [Listeners](#listeners)
- [Contributing](#contributing)

## Getting started

1. Install the package

Maven
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
             <version>1.13.0-Release</version>
            <scope>compile</scope>
        </dependency>
   </dependencies>
```

Gradle
```gradle
dependencyResolutionManagement {
		repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
		repositories {
			mavenCentral()
			maven { url 'https://jitpack.io' }
		}
	}

dependencies {
	        implementation 'com.github.jwdeveloper.TikTok-Live-Java:Client:1.13.0-Release'
	}
```

2. Create your first chat connection

```java

TikTokLive.newClient("bangbetmenygy")
        .onGift((liveClient, event) ->
        {
            String message = switch (event.getGift()) {
                case ROSE -> "ROSE!";
                case GG -> "GOOD GAME";
                case TIKTOK -> "Ye";
                case CORGI -> "Nice gift";
                default -> "Thank you for " + event.getGift().getName();
            };
            System.out.println(event.getUser().getProfileName() + " sends " + message);
        })
        .onGiftCombo((liveClient, event) ->
        {
            System.out.println(event.getComboState()+ " " + event.getCombo() + " " + event.getGift().getName());
        })
        .onRoomInfo((liveClient, event) ->
        {
            var roomInfo = event.getRoomInfo();
            System.out.println("Room Id: "+roomInfo.getRoomId());
            System.out.println("Likes: "+roomInfo.getLikesCount());
            System.out.println("Viewers: "+roomInfo.getViewersCount());
        })
        .onJoin((liveClient, event) ->
        {
            System.out.println(event.getUser().getProfileName() + "Hello on my stream! ");
        })
        .onConnected((liveClient, event) ->
        {
            System.out.println("Connected to live ");
        })
        .onError((liveClient, event) ->
        {
            System.out.println("Error! " + event.getException().getMessage());
        })
        .buildAndConnect();

```
3. Configure (optional)

```java

        TikTokLive.newClient("bangbetmenygy")
                .configure((settings) ->
                {
                    settings.setHostName("bangbetmenygy"); // This method is useful in case you want change hostname later
                    settings.setClientLanguage("en"); // Language
                    settings.setTimeout(Duration.ofSeconds(2)); // Connection timeout
                    settings.setLogLevel(Level.ALL); // Log level
                    settings.setPrintToConsole(true); // Printing all logs to console even if log level is Level.OFF
                    settings.setRetryOnConnectionFailure(true); // Reconnecting if TikTok user is offline
                    settings.setRetryConnectionTimeout(Duration.ofSeconds(1)); // Timeout before next reconnection

                    //Optional: Sometimes not every message from chat are send to TikTokLiveJava to fix this issue you can set sessionId
                    // documentation how to obtain sessionId https://github.com/zerodytrash/TikTok-Live-Connector#send-chat-messages
                    settings.setSessionId("86c3c8bf4b17ebb2d74bb7fa66fd0000");

                    //Optional:
                    //RoomId can be used as an override if you're having issues with HostId.
                    //You can find it in the HTML for the livestream-page
                    settings.setRoomId("XXXXXXXXXXXXXXXXX");
                })
                .buildAndConnect();
        //  
```


## Events



## Events



- [Documentation](https://ttlj.docs.isaackogan.com/)

**Control**:

- [onReconnecting](#onreconnecting-tiktokreconnectingevent) · [docs](https://ttlj.docs.isaackogan.com/events/control/#tiktokreconnectingevent)
- [onError](#onerror-tiktokerrorevent) · [docs](https://ttlj.docs.isaackogan.com/events/control/#tiktokerrorevent)
- [onConnected](#onconnected-tiktokconnectedevent) · [docs](https://ttlj.docs.isaackogan.com/events/control/#tiktokconnectedevent)
- [onDisconnected](#ondisconnected-tiktokdisconnectedevent) · [docs](https://ttlj.docs.isaackogan.com/events/control/#tiktokdisconnectedevent)

**Message**:

- [onEvent](#onevent-tiktokevent) · [docs](https://ttlj.docs.isaackogan.com/events/)
- [onEvent](#onevent-tiktokevent) · [docs](https://ttlj.docs.isaackogan.com/events/)
- [onComment](#oncomment-tiktokcommentevent) · [docs](https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktokcommentevent)
- [onRoomInfo](#onroominfo-tiktokroominfoevent) · [docs](https://ttlj.docs.isaackogan.com/events/room-and-display/#tiktokroominfoevent)
- [onGift](#ongift-tiktokgiftevent) · [docs](https://ttlj.docs.isaackogan.com/events/gifts-and-goals/#tiktokgiftevent)
- [onSubscribe](#onsubscribe-tiktoksubscribeevent) · [docs](https://ttlj.docs.isaackogan.com/events/subscriptions-and-fans/#tiktoksubscribeevent)
- [onFollow](#onfollow-tiktokfollowevent) · [docs](https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktokfollowevent)
- [onGiftCombo](#ongiftcombo-tiktokgiftcomboevent) · [docs](https://ttlj.docs.isaackogan.com/events/gifts-and-goals/#tiktokgiftcomboevent)
- [onLiveEnded](#onliveended-tiktokliveendedevent) · [docs](https://ttlj.docs.isaackogan.com/events/stream-control/#tiktokliveendedevent)
- [onQuestion](#onquestion-tiktokquestionevent) · [docs](https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktokquestionevent)
- [onShare](#onshare-tiktokshareevent) · [docs](https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktokshareevent)
- [onLiveUnpaused](#onliveunpaused-tiktokliveunpausedevent) · [docs](https://ttlj.docs.isaackogan.com/events/stream-control/#tiktokliveunpausedevent)
- [onEmote](#onemote-tiktokemoteevent) · [docs](https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktokemoteevent)
- [onJoin](#onjoin-tiktokjoinevent) · [docs](https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktokjoinevent)
- [onLike](#onlike-tiktoklikeevent) · [docs](https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktoklikeevent)
- [onLivePaused](#onlivepaused-tiktoklivepausedevent) · [docs](https://ttlj.docs.isaackogan.com/events/stream-control/#tiktoklivepausedevent)

**Debug**:

- [onWebsocketResponse](#onwebsocketresponse-tiktokwebsocketresponseevent) · [docs](https://ttlj.docs.isaackogan.com/events/debugging/#tiktokwebsocketresponseevent)
- [onWebsocketUnhandledMessage](#onwebsocketunhandledmessage-tiktokwebsocketunhandledmessageevent) · [docs](https://ttlj.docs.isaackogan.com/events/debugging/#tiktokwebsocketunhandledmessageevent)
- [onHttpResponse](#onhttpresponse-tiktokhttpresponseevent) · [docs](https://ttlj.docs.isaackogan.com/events/debugging/#tiktokhttpresponseevent)
- [onWebsocketMessage](#onwebsocketmessage-tiktokwebsocketmessageevent) · [docs](https://ttlj.docs.isaackogan.com/events/debugging/#tiktokwebsocketmessageevent)
# Examples
<br>

## onReconnecting [TikTokReconnectingEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/TikTokReconnectingEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/control/#tiktokreconnectingevent



```java
TikTokLive.newClient("host-name")
.onReconnecting((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onError [TikTokErrorEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/TikTokErrorEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/control/#tiktokerrorevent


General error event. You should handle this.


```java
TikTokLive.newClient("host-name")
.onError((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onConnected [TikTokConnectedEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/TikTokConnectedEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/control/#tiktokconnectedevent


Triggered when the connection is successfully established.


```java
TikTokLive.newClient("host-name")
.onConnected((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onDisconnected [TikTokDisconnectedEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/TikTokDisconnectedEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/control/#tiktokdisconnectedevent


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

## onEvent [TikTokEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/common/TikTokEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/


Base class for all events


```java
TikTokLive.newClient("host-name")
.onEvent((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onEvent [TikTokEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/common/TikTokEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/


Base class for all events


```java
TikTokLive.newClient("host-name")
.onEvent((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onComment [TikTokCommentEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/TikTokCommentEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktokcommentevent


Triggered every time a new chat comment arrives.


```java
TikTokLive.newClient("host-name")
.onComment((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onRoomInfo [TikTokRoomInfoEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/room/TikTokRoomInfoEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/room-and-display/#tiktokroominfoevent


Triggered when LiveRoomInfo got updated such as likes, viewers, ranking ....


```java
TikTokLive.newClient("host-name")
.onRoomInfo((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onGift [TikTokGiftEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/gift/TikTokGiftEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/gifts-and-goals/#tiktokgiftevent


Triggered when user sends gifts that has
no combo (most of expensive gifts)
or if combo has finished


```java
TikTokLive.newClient("host-name")
.onGift((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onSubscribe [TikTokSubscribeEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/TikTokSubscribeEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/subscriptions-and-fans/#tiktoksubscribeevent


Triggers when a user creates a subscription.


```java
TikTokLive.newClient("host-name")
.onSubscribe((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onFollow [TikTokFollowEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/social/TikTokFollowEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktokfollowevent


Triggers when a user follows the streamer. Based on social event.


```java
TikTokLive.newClient("host-name")
.onFollow((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onGiftCombo [TikTokGiftComboEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/gift/TikTokGiftComboEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/gifts-and-goals/#tiktokgiftcomboevent


Triggered every time gift is sent

@see GiftSendType it has 3 states

  <p>Example when user sends gift with combo</p>
  <p>>Combo: 1  -> comboState = GiftSendType.Begin</p>
  <p>Combo: 4 -> comboState = GiftSendType.Active</p>
  <p>Combo: 8 -> comboState = GiftSendType.Active</p>
  <p>Combo: 12 -> comboState = GiftSendType.Finished</p>
  <p>
  Remember if comboState is Finished both TikTokGiftComboEvent and TikTokGiftEvent event gets triggered


```java
TikTokLive.newClient("host-name")
.onGiftCombo((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onLiveEnded [TikTokLiveEndedEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/TikTokLiveEndedEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/stream-control/#tiktokliveendedevent


Triggered when the live stream gets terminated by the host. Will also trigger the TikTokDisconnectedEvent event.


```java
TikTokLive.newClient("host-name")
.onLiveEnded((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onQuestion [TikTokQuestionEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/TikTokQuestionEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktokquestionevent


Triggered every time someone asks a new question via the question feature.


```java
TikTokLive.newClient("host-name")
.onQuestion((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onShare [TikTokShareEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/social/TikTokShareEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktokshareevent


Triggers when a user shares the stream. Based on social event.


```java
TikTokLive.newClient("host-name")
.onShare((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onLiveUnpaused [TikTokLiveUnpausedEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/TikTokLiveUnpausedEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/stream-control/#tiktokliveunpausedevent



```java
TikTokLive.newClient("host-name")
.onLiveUnpaused((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onEmote [TikTokEmoteEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/TikTokEmoteEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktokemoteevent


Triggered every time a subscriber sends an emote (sticker).


```java
TikTokLive.newClient("host-name")
.onEmote((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onJoin [TikTokJoinEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/social/TikTokJoinEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktokjoinevent



```java
TikTokLive.newClient("host-name")
.onJoin((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onLike [TikTokLikeEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/social/TikTokLikeEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/chat-and-audience/#tiktoklikeevent


Triggered when a viewer sends likes to the streamer. For streams with many viewers, this event is not always triggered by TikTok.


```java
TikTokLive.newClient("host-name")
.onLike((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onLivePaused [TikTokLivePausedEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/TikTokLivePausedEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/stream-control/#tiktoklivepausedevent



```java
TikTokLive.newClient("host-name")
.onLivePaused((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onWebsocketResponse [TikTokWebsocketResponseEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/websocket/TikTokWebsocketResponseEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/debugging/#tiktokwebsocketresponseevent



```java
TikTokLive.newClient("host-name")
.onWebsocketResponse((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onWebsocketUnhandledMessage [TikTokWebsocketUnhandledMessageEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/websocket/TikTokWebsocketUnhandledMessageEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/debugging/#tiktokwebsocketunhandledmessageevent


Triggered every time a protobuf encoded webcast message arrives. You can deserialize the binary object depending on the use case.


```java
TikTokLive.newClient("host-name")
.onWebsocketUnhandledMessage((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onHttpResponse [TikTokHttpResponseEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/http/TikTokHttpResponseEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/debugging/#tiktokhttpresponseevent



```java
TikTokLive.newClient("host-name")
.onHttpResponse((liveClient, event) ->
{

})
.buildAndConnect();
```



<br>

## onWebsocketMessage [TikTokWebsocketMessageEvent](https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/api/data/events/websocket/TikTokWebsocketMessageEvent.java)

Documentation: https://ttlj.docs.isaackogan.com/events/debugging/#tiktokwebsocketmessageevent


Triggered every time TikTok sends data. Data incoming as protobuf message.
You can deserialize the binary object depending on the use case.


```java
TikTokLive.newClient("host-name")
.onWebsocketMessage((liveClient, event) ->
{

})
.buildAndConnect();
```






<br>


## Extensions

List of extensions (addons) to TiktokLiveJava
that will save your time

- [Video Recorder](https://github.com/jwdeveloper/TikTokLiveJava/tree/master/extension-recorder)
- [Live data collector to database](https://github.com/jwdeveloper/TikTokLiveJava/tree/master/extension-collector)



## Listeners

```java

/**
 *
 *  Listeners are an alternative way of handling events.
 *  I would to suggest to use then when logic of handing event
 *  is more complex
 *
 */
public static void main(String[] args) throws IOException {
        showLogo();
        CustomListener customListener = new CustomListener();

        TikTokLive.newClient(SimpleExample.TIKTOK_HOSTNAME)
        .addListener(customListener)
        .buildAndConnect();
        System.in.read();
        }

/**
 *
 *  Method in TikTokEventListener should meet 4 requirements to be detected
 *         - must have @TikTokEventHandler annotation
 *         - must have 2 parameters
 *         - first parameter must be LiveClient
 *         - second must be class that extending TikTokEvent
 */

public static class CustomListener {

    @TikTokEventObserver
    public void onLike(LiveClient liveClient, TikTokLikeEvent event) {
        System.out.println(event.toString());
    }

    @TikTokEventObserver
    public void onError(LiveClient liveClient, TikTokErrorEvent event) {
        //  event.getException().printStackTrace();
    }

    @TikTokEventObserver
    public void onComment(LiveClient liveClient, TikTokCommentEvent event) {
        var userName = event.getUser().getName();
        var text = event.getText();
        liveClient.getLogger().info(userName + ": " + text);
    }

    @TikTokEventObserver
    public void onGift(LiveClient liveClient, TikTokGiftEvent event) {
        var message = switch (event.getGift()) {
            case ROSE -> "Thanks :)";
            case APPETIZERS -> ":OO";
            case APRIL -> ":D";
            case TIKTOK -> ":P";
            case CAP -> ":F";
            default -> ":I";
        };
        liveClient.getLogger().info(message);
    }

    @TikTokEventHandler
    public void onAnyEvent(LiveClient liveClient, TikTokEvent event) {
        liveClient.getLogger().info(event.getClass().getSimpleName());
    }

}

```


## Contributing

[Library documentation for contributors](https://github.com/jwdeveloper/TikTokLiveJava/wiki)

Your improvements are welcome! Feel free to open an <a href="https://github.com/jwdeveloper/TikTok-Live-Java/issues">issue</a> or <a href="https://github.com/jwdeveloper/TikTok-Live-Java/pulls">pull request</a>.
