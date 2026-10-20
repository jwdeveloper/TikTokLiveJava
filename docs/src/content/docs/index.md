---
title: Getting Started
description: Install TikTokLiveJava and receive comments, gifts, and other events from a TikTok livestream with Java.
head:
  - tag: title
    content: "TikTok LIVE API for Java: TikTokLiveJava Documentation"
---

TikTokLiveJava receives comments, gifts, likes, follows, shares, viewer counts, battles, and other events from a TikTok livestream. Create a client for a creator's username with `TikTokLive.newClient`, register a handler for each event you need, and connect.

## Prerequisites

- Java 16 or newer and a Maven or Gradle project. The library is published on JitPack, not Maven Central, so the build needs the JitPack repository; see [Install](/getting-started/install/).
- No TikTok login for public livestreams. Signing uses a third-party provider, [Euler Stream](https://www.eulerstream.com/) by default. Age-restricted rooms and sending chat messages need a TikTok session (`sessionId` and `ttTargetIdc`); see [client settings](/configuration/client-settings/).

This is an unofficial reverse-engineering project, not a supported vendor API. TikTok can change the Webcast protocol without notice, and a defined event may not be sent in every room or region. The [protobuf schema](/schema/) lists what the protocol defines; the [event catalog](/events/) lists what the client emits.

## Install

Maven:

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
        <version>1.13.1-Release</version>
    </dependency>
</dependencies>
```

Gradle:

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.jwdeveloper.TikTok-Live-Java:Client:1.13.1-Release'
}
```

[Install](/getting-started/install/) covers the second JitPack group name, versions, and a check that the dependency resolves.

## Listen to a livestream

Save this as `ChatReader.java`, replace `isaackogz` with a creator who is live, and run it.

```java
import io.github.jwdeveloper.tiktok.client.TikTokLive;

public class ChatReader {
    public static void main(String[] args) {
        TikTokLive.newClient("isaackogz")
                .onConnected((client, event) ->
                        System.out.println("Connected to " + client.getRoomInfo().getHostName()))
                .onComment((client, event) ->
                        System.out.println(event.getUser().getProfileName() + ": " + event.getText()))
                .onGift((client, event) ->
                        System.out.println(event.getUser().getProfileName() + " sent " + event.getGift().getName() + " x" + event.getCombo()))
                .onDisconnected((client, event) ->
                        System.out.println("Disconnected: " + event.getReason()))
                .buildAndConnect();
    }
}
```

`TikTokLive.newClient` takes the creator's username, the part after `@` in `https://www.tiktok.com/@isaackogz/live`; a leading `@` is removed. Each `on<Event>` method of the builder registers a handler for one event class, called with the `LiveClient` and the event; `onEvent` receives every event, and `addListener` registers an object whose methods handle events ([listeners](/guides/listeners/)). Register handlers before connecting: the signing response already carries messages, and they are dispatched before the socket opens.

`buildAndConnect()` builds the client, connects, and returns the `LiveClient`. It fetches the room data, requests a signed WebSocket URL, starts the socket, and returns; `TikTokConnectedEvent` fires from the socket thread once the socket is open. That thread keeps the process alive until the connection closes; call `client.disconnect()` to close it. When the creator is offline, `buildAndConnect()` throws `TikTokLiveOfflineHostException`. `TikTokLive.isLiveOnline("isaackogz")` checks first, and `buildAndConnectAsync()` connects on another thread. The [connection lifecycle](/getting-started/connection/) covers both, the retry settings, and each lifecycle event.

## Read event payloads

Each event is a class under `io.github.jwdeveloper.tiktok.api.data.events`, mapped from a Webcast protobuf message: `TikTokCommentEvent` from `WebcastChatMessage`, `TikTokGiftEvent` from `WebcastGiftMessage`. For a comment, the text is `event.getText()` and the sender is `event.getUser()`, a `User` whose `getName()` is the username and `getProfileName()` the display name. For a gift, `event.getGift().getName()` names the gift and `event.getCombo()` is the streak count. Nested objects such as `getUser()` are mapped from the message and are not `null`; a user TikTok did not send has id `0` and an empty name. Events with a message header also carry `getRoomId()`, `getMessageId()`, and `getTimeStamp()`.

The [event catalog](/events/) lists each event with its type and fields, the [API reference](/api/) documents every public class in this checkout, and the [schema reference](/schema/) describes the protobuf messages the events are mapped from.

## Signing

The client requests its WebSocket URL from Euler Stream, a hosted [TikTok LIVE signing API](https://www.eulerstream.com/), at `tiktok.eulerstream.com`. A TikTok login is unnecessary for public rooms; provider rate limits still apply, and an API key raises them. Set `apiKey` in `configure`, and the client sends it as the `x-api-key` header. A rate-limited or failed signing request throws `TikTokSignServerException` from `connect()`. For production workloads that need an uptime guarantee, the managed [TikTok LIVE WebSocket API](https://www.eulerstream.com/websockets) handles signing, scaling, and protocol changes for you: set `useEulerstreamWebsocket` to `true` together with the API key, and the client receives its events from `wss://ws.eulerstream.com` instead of connecting to TikTok itself. See [signing](/configuration/signing/) and [troubleshooting](/troubleshooting/).

```java
TikTokLive.newClient("isaackogz")
        .configure(settings -> settings.setApiKey("your-euler-stream-api-key"))
        .onComment((client, event) -> System.out.println(event.getText()))
        .buildAndConnect();
```

## Next steps

- [Connection lifecycle](/getting-started/connection/): `build`, `buildAndConnect`, and `buildAndConnectAsync`, the connect sequence, lifecycle events, disconnects, retries, and room information.
- [Event catalog](/events/): find an event by its name or what it does.
- [Gifts and combos](/guides/gifts/): `TikTokGiftEvent`, `TikTokGiftComboEvent`, and gift streaks.
- [Client settings](/configuration/client-settings/): every `LiveClientSettings` field and its default.
- [API reference](/api/): generated from the Java sources in this checkout.

These docs describe the library on `master`. The guides and the generated API reference come from the same checkout, so compare your dependency version with the version on the [API reference](/api/) index when something differs from your installed release.

For the Node.js implementation, see [TikTok LIVE Connector](https://ttlc.docs.isaackogan.com/); for the Python implementation, see [TikTokLive](https://ttl.docs.isaackogan.com/). For the managed Euler Stream service, see [Euler Stream's documentation](https://www.eulerstream.com/docs). Their names and payload shapes differ from this Java library.
