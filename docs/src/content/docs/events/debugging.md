---
title: Debugging
description: The debug events that expose every batch, every message with the event it became, and messages without a mapping, plus the HTTP response event and the generic base classes.
sidebar:
  order: 11
---

The four `Debug` events show the traffic behind the message events. For every batch of messages the client processes, from the signing response and from each WebSocket frame, it publishes `TikTokWebsocketResponseEvent`, then for each message in the batch either a `TikTokWebsocketMessageEvent` before each event the message maps to, or a `TikTokWebsocketUnhandledMessageEvent` when no mapping is registered for the message's name. A message `publishMessage` injects goes through the same mapping without a batch event. A message whose mapping throws is published as `TikTokErrorEvent` with a `TikTokLiveMessageException` instead.

Each debug event has a builder method, named below. Replace `isaackogz` with a creator who is live.

## TikTokWebsocketResponseEvent

A batch of messages arrived, before any of them is mapped. Handler: `onWebsocketResponse`. No Webcast message of its own; it wraps the batch.

Key field: `getResponse()`, the [`ProtoMessageFetchResult`](/schema/webcast/protomessagefetchresult/) with `getMessagesList()`, the batch, and the cursor and timing fields the schema page lists.

## TikTokWebsocketMessageEvent

Triggered every time TikTok sends data, with the message, its event, and the mapping time. Handler: `onWebsocketMessage`. No Webcast message of its own; it wraps one.

Key fields: `getMessage()`, the [`BaseProtoMessage`](/schema/webcast/protomessagefetchresult-baseprotomessage/) with `getMethod()`, the message's name, and `getPayload()`, its protobuf bytes; `getEvent()`, the event the message was mapped to, published right after this one; and `getMetaData()`, whose `getHandlingTime()` is the mapping's duration. A message that maps to two events, such as a like message, produces two of these, each wrapping one event.

```java
TikTokLive.newClient("isaackogz")
        .onWebsocketUnhandledMessage((client, event) -> System.out.println("No mapping for " + event.getMessage().getMethod()))
        .onWebsocketMessage((client, event) -> System.out.println(event.getMessage().getMethod() + " -> " + event.getEvent().getClass().getSimpleName() + " in " + event.getMetaData().getHandlingTime().toNanos() + " ns"))
        .buildAndConnect();
```

## TikTokWebsocketUnhandledMessageEvent

A protobuf-encoded Webcast message arrived that no mapping is registered for; you can deserialize the binary payload yourself. Handler: `onWebsocketUnhandledMessage`. No Webcast message of its own; it wraps one.

Key field: `getMessage()`, the [`BaseProtoMessage`](/schema/webcast/protomessagefetchresult-baseprotomessage/) with `getMethod()` and `getPayload()`; `getData()` returns the same object. The method name tells you which message TikTok sent, and the [schema reference](/schema/) whether the library has a class for it; a custom mapping turns it into an event, as the [catalog](/events/#unmapped-events) shows. This event, not `TikTokErrorEvent`, is where a message type the library does not know ends up, so a handler that logs the method names is the quickest way to see what a room sends.

## TikTokHttpResponseEvent

An HTTP request and its response. Handler: `onHttpResponse`. No Webcast message.

Key fields: `getUrl()`, `getRequest()`, and `getResponse()`, the last two as `HttpData` with `getUrl()`, `getMethod()`, `getHeaders()`, `getParameters()`, `getStatus()`, and `getBody()`. No class in this checkout publishes the event: the handler and `TikTokEventListenerBase.onHttpResponse` exist, but the HTTP client does not call them. It can only reach a handler through your own `publishEvent`.

## TikTokUnhandledEvent

The generic base class of the unhandled wrappers, holding the raw message. It has no `@EventMeta` category and one field, `getData()`, typed by the subclass: `TikTokWebsocketUnhandledMessageEvent` above, and `TikTokUnhandledMemberEvent`, `TikTokUnhandledSocialEvent`, and `TikTokUnhandledControlEvent` on [moderation and access](/events/moderation-and-access/). The client never publishes it on its own, and a handler for `TikTokUnhandledEvent.class` receives none of the subclasses.

## TikTokEmptyEvent

An event with no fields. It has no `@EventMeta` category and nothing in the library references it; it is a placeholder for your own `publishEvent` calls, for instance to trigger a listener from a test.
