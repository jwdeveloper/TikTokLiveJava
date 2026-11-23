---
title: Offline testing
description: Build a client in offline mode and drive handlers and listeners with publishEvent and publishMessage, the of factories for test events, replaying recorded Webcast payloads, and what offline mode cannot do.
sidebar:
  order: 6
---

`offline` on `LiveClientSettings` makes `build()` wire stand-in HTTP and WebSocket clients, so no request leaves the process, and `publishEvent` and `publishMessage` then drive handlers and listeners exactly as live traffic would. Do not call `connect()` on an offline client: in this release it fails before the stand-in socket starts, see [offline mode](/configuration/client-settings/#offline-mode); `build()` is all a test needs. This guide is based on the repository's `Events_And_Gifts_Testing_Example.java`.

## Publish events

`publishEvent(event)` runs the handlers for the event's exact class and the `TikTokEvent` catch-all, on the calling thread, before it returns; an exception thrown by a handler propagates to the caller. Several event classes have `of` factories that build an event without a Webcast message:

| Factory | Builds |
| --- | --- |
| `TikTokCommentEvent.of(userName, text)` | A comment whose `getUser().getName()` is `userName` |
| `TikTokLikeEvent.of(userName, likes)`, `of(user, likes)` | A like event with that count |
| `TikTokFollowEvent.of(userName)`, `of(user)` | A follow |
| `TikTokShareEvent.of(userName, shares)`, `of(user, shares)` | A share |
| `TikTokJoinEvent.of(userName)`, `of(user)` | A join |
| `TikTokSubscribeEvent.of(userName)` | A subscription |
| `TikTokGiftEvent.of(gift)`, `of(name, id, diamonds)`, `of(gift, user, toUser)` | A gift; see [gifts](/guides/gifts/#test-gift-handlers-offline) |
| `TikTokGiftComboEvent.of(gift, combo, state)`, `of(gift, host, user, combo, state)` | A streak frame with that count and `GiftComboStateType` |
| `TikTokConnectedEvent.of()`, `TikTokConnectingEvent.of()`, `TikTokLiveEndedEvent.of()` | The control event with no fields |
| `TikTokErrorEvent.of(message)` | An error carrying a `TikTokLiveException` with that message |

Any other message event has a constructor that takes its Webcast message, which the protobuf builders can create: `new TikTokCommentEvent(WebcastChatMessage.newBuilder().setContent("hi").build())` is a comment from a user with id `0` and an empty name. Fields the builder leaves unset read as protobuf defaults, not `null`.

```java
var client = TikTokLive.newClient("isaackogz")
        .configure(settings -> settings.setOffline(true))
        .onComment((liveClient, event) ->
                System.out.println(event.getUser().getName() + ": " + event.getText()))
        .onLike((liveClient, event) ->
                System.out.println(event.getUser().getName() + " sent " + event.getLikes() + " likes"))
        .onFollow((liveClient, event) ->
                System.out.println(event.getUser().getName() + " followed"))
        .onGift((liveClient, event) ->
                System.out.println(event.getCombo() + " x " + event.getGift().getName()))
        .build();

client.publishEvent(TikTokCommentEvent.of("mark", "Hello world"));
client.publishEvent(TikTokLikeEvent.of("mark", 12));
client.publishEvent(TikTokFollowEvent.of("mark"));
client.publishEvent(TikTokGiftEvent.of("Rose", 5655, 1));
```

Listeners are tested the same way: add them on the builder or through `getListenersManager()`, then publish; see [listeners](/guides/listeners/). A listener method that does not use its `LiveClient` parameter can also be called directly from a test with `null`, without a client at all.

## Replay recorded messages

`publishMessage(name, payloadBase64)` and `publishMessage(name, payload)` wrap the bytes in a Webcast message with that name and run it through the mapper, as a message from the socket would be: a name with no mapping publishes `TikTokWebsocketUnhandledMessageEvent`; a mapped one publishes a `TikTokWebsocketMessageEvent` and then the event, for each event the mapping returns. No `TikTokWebsocketResponseEvent` is published, since there is no batch, and a mapping that throws is not reported as a `TikTokErrorEvent` but thrown to the caller, so a bad payload fails the test at the `publishMessage` call. Custom mappings registered with `mappings` apply; see [custom events and mappings](/guides/custom-events/).

Payloads come from three places: a handler on `onWebsocketMessage` or `onWebsocketUnhandledMessage` that encodes `event.getMessage().getPayload().toByteArray()` with `Base64.getEncoder()`, the [collector](/guides/collector/), whose `message` documents hold the name and the Base64 payload, and the repository's example, which carries a `WebcastLikeMessage` payload. A message can also be built in code with the protobuf builders.

```java
import io.github.jwdeveloper.tiktok.api.messages.webcast.WebcastChatMessage;

var client = TikTokLive.newClient("isaackogz")
        .configure(settings -> settings.setOffline(true))
        .onWebsocketMessage((liveClient, event) ->
                System.out.println(event.getMessage().getMethod() + " -> " + event.getEvent().getClass().getSimpleName()))
        .onWebsocketUnhandledMessage((liveClient, event) ->
                System.out.println("No mapping for " + event.getMessage().getMethod()))
        .onComment((liveClient, event) -> System.out.println("Comment: " + event.getText()))
        .onLike((liveClient, event) -> System.out.println("Likes: " + event.getLikes() + ", total " + event.getTotalLikes()))
        .build();

var chat = WebcastChatMessage.newBuilder().setContent("Replayed from bytes").setContentLanguage("en").build();
client.publishMessage("WebcastChatMessage", chat.toByteArray());

client.publishMessage("WebcastLikeMessage", "EAUYZA==");

client.publishMessage("WebcastNotYetKnownMessage", new byte[0]);
```

The Base64 string is a `WebcastLikeMessage` with `count` 5 and `total` 100, built with the same builders and encoded; a like message maps to a `TikTokLikeEvent` and then a `TikTokRoomInfoEvent`, so the handler above runs after one `TikTokWebsocketMessageEvent` for each.

## What offline mode cannot do

- `connect()` fails before the stand-in socket starts, so no `TikTokConnectedEvent` or `TikTokDisconnectedEvent` comes from the lifecycle. A handler that waits for one gets it from `publishEvent(TikTokConnectedEvent.of())`.
- `getRoomInfo()` holds the `hostName` and nothing else: `getRoomId()` and `getHost()` are `null`. The gift mapping uses `getHost()` as the recipient of a gift message that names none, so a `WebcastGiftMessage` replayed offline has a `null` `getToUser()` unless the message sets `toUser`.
- The gift manager is empty, since no gift list is fetched; attach the gifts a test needs, see [gifts](/guides/gifts/#test-gift-handlers-offline).
- `sendChat` returns `false` without sending.
- `TikTokLive.isLiveOnline` and `isHostNameValid` are static and make their requests regardless of any client's settings.
