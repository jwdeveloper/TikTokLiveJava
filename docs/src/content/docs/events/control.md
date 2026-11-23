---
title: Control events
description: The six events the client publishes about its own connection, connecting, pre-connection, connected, reconnecting, disconnected, and error, with their fields and when they fire.
sidebar:
  order: 1
---

Control events come from the client rather than from a Webcast message. They mark the connection's lifecycle and carry no message header; all six extend `TikTokLiveClientEvent`. The end of a stream is not a control event: TikTok reports it in a message, and the client publishes `TikTokLiveEndedEvent` without disconnecting; see [stream control](/events/stream-control/). The [connection lifecycle](/getting-started/connection/) page walks through the connect sequence that publishes these events, the retry settings, and the Euler Stream WebSocket path, on which `TikTokPreConnectionEvent` is not published.

Every control event has a builder method, named below. Replace `isaackogz` with a creator who is live.

## TikTokConnectingEvent

The first thing `connect()` does, before any request to TikTok or the signing provider. Handler: `onConnecting`. No message, no fields.

It is published on both connection paths. On the default path the user data and room data are fetched next; on the Euler Stream WebSocket path the socket is opened directly.

## TikTokPreConnectionEvent

The room data is fetched and the connection can still be cancelled. Handler: `onPreConnection`. No message; the fields hold the two HTTP responses `connect()` has made so far.

Key fields: `getUserData()`, the `LiveUserData.Response` for the creator; `getRoomData()`, the `LiveData.Response` for the room, with `isAgeRestricted()` among its fields; `setCancelConnection(boolean, String)`, which aborts the connection with the given reason; `isCancelConnection()` and `getReason()`, the current values. A cancelled connection throws `TikTokLivePreConnectionException` from `connect()`, which then publishes `TikTokErrorEvent` and `TikTokDisconnectedEvent` like any other failure. The event is published after `getRoomInfo()` has been filled and before the gift list and the signed URL are requested; it is not published with `useEulerstreamWebsocket`.

## TikTokConnectedEvent

Triggered when the connection is successfully established. Handler: `onConnected`. No message, no fields.

The client publishes it from the socket thread once the WebSocket is open, which can be before or after `connect()` returns. On the default path `getRoomInfo()` is already filled; on the Euler Stream WebSocket path it is still empty until the first `TikTokRoomInfoEvent`.

```java
TikTokLive.newClient("isaackogz")
        .onConnected((client, event) -> System.out.println("Connected to room " + client.getRoomInfo().getRoomId()))
        .buildAndConnect();
```

## TikTokReconnectingEvent

A scheduled retry is about to call `connect()` again. Handler: `onReconnecting`. No message, no fields.

It is published only by the client's own retry, which runs when `retryOnConnectionFailure` is `true` and `connect()` failed because the creator was offline; the wait before each attempt is `retryConnectionTimeout`. A socket that drops after connecting is not retried, so this event never follows a `TikTokDisconnectedEvent` of a live connection. See [client settings](/configuration/client-settings/).

## TikTokDisconnectedEvent

Triggered when the connection gets disconnected. Handler: `onDisconnected`. No message.

Key fields: `getCode()`, the WebSocket close code when TikTok or `disconnect()` closed the socket, or `TikTokDisconnectedEvent.UNKNOWN_CLOSE_CODE` (`-1`) when a connect attempt failed; `isUnknownCloseCode()`, `true` in that second case; and `getReason()`, the close frame's reason, `None` when it carried none, or `Exception: <message>` after a failed connect. The same client can `connect()` again after a disconnect; wait before doing so, since a tight loop runs into the signing provider's rate limit.

```java
TikTokLive.newClient("isaackogz")
        .onDisconnected((client, event) -> {
            if (event.isUnknownCloseCode()) {
                System.out.println("Connect attempt failed: " + event.getReason());
            } else {
                System.out.println("Socket closed with code " + event.getCode() + ": " + event.getReason());
            }
        })
        .buildAndConnect();
```

## TikTokErrorEvent

General error event. You should handle this. Handler: `onError`. No message.

Key field: `getException()`, a `Throwable`. It is a `TikTokLiveException` or one of its subclasses when `connect()` failed, after which `TikTokDisconnectedEvent` follows; a `TikTokLiveMessageException` when a message could not be mapped or a handler threw while the message was processed, in which case the client carries on with the next message; and a `TikTokEventListenerMethodException` when a listener method threw. The client does not log these exceptions itself, so an `onError` handler that logs the exception is the usual first step in [troubleshooting](/troubleshooting/).

## TikTokLiveClientEvent

The base class of the six control events. It has no `@EventMeta` category, no fields, and the client never publishes it on its own; subscribe to the concrete events. The abstract `TikTokEvent` above it is the root of every event and the parameter type of the catch-all `onEvent(handler)`.
