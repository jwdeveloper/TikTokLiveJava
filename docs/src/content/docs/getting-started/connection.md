---
title: Connection lifecycle
description: Build and connect a client, follow the connect sequence and its lifecycle events, disconnect, retry an offline creator, read room information, and check whether a creator is live.
---

`TikTokLive.newClient(hostName)` returns a `LiveClientBuilder`. Its `on<Event>` methods register handlers, `configure` edits the client's `LiveClientSettings`, and one of three terminal methods produces the `LiveClient`.

## Build, connect, or both

| Method | Returns | Use it when |
| --- | --- | --- |
| `build()` | The `LiveClient`, not connected | You want to keep the client, register listeners through `getListenersManager()`, or connect later. |
| `buildAndConnect()` | The connected `LiveClient` | A program that connects once. It throws when the connection fails. |
| `buildAndConnectAsync()` | A `CompletableFuture<LiveClient>` | The calling thread must not wait. The future completes with the client, or exceptionally when the connection fails. |

`LiveClient` has the matching methods. `connect()` blocks the calling thread while the client fetches the room data, signs, and starts the socket. `connectAsync()` runs `connect()` on the common pool and returns a `CompletableFuture<LiveClient>`; `connectAsync(Consumer<LiveClient>)` passes the connected client to the consumer and discards a failure: the consumer is not called and nothing is thrown. One connection per client: calling `connect()` while the state is `CONNECTING` or `CONNECTED` throws `TikTokLiveException` with the message `Already connected`. That exception goes through the same failure branch as any other: the client sets the state to `DISCONNECTED` and publishes `TikTokErrorEvent` and `TikTokDisconnectedEvent` with the reason `Exception: Already connected`, while the existing socket stays open and keeps delivering events. The state no longer matches the socket, and the next `connect()` passes the state check and replaces the socket, closing the old one first. After a disconnect the same client can `connect()` again.

```java
var client = TikTokLive.newClient("isaackogz")
        .onComment((liveClient, event) -> System.out.println(event.getText()))
        .build();

client.connect();
```

The common pool's threads are daemon threads, and the socket's read thread inherits that flag from the thread that called `connect()`, so after `buildAndConnectAsync()` keep a non-daemon thread alive for as long as the connection should live; the repository's `ConnectionExample` blocks on `System.in.read()`. See the [WebSocket layer](/advanced/websocket/#connection-sequence).

```java
TikTokLive.newClient("isaackogz")
        .onComment((client, event) -> System.out.println(event.getText()))
        .buildAndConnectAsync()
        .thenAccept(client -> System.out.println("Connected to " + client.getRoomInfo().getHostName()))
        .exceptionally(error -> {
            System.err.println("Connection failed: " + error.getMessage());
            return null;
        });

System.in.read();
```

## The connect sequence

In order, `connect()`:

1. Sets the state to `CONNECTING` and publishes `TikTokConnectingEvent`.
2. Fetches the creator's user data from TikTok. An offline creator throws `TikTokLiveOfflineHostException`; an unknown username throws `TikTokLiveUnknownHostException`.
3. Fetches the room's live data and fills `getRoomInfo()` with the room id, title, host, viewer counts, and age restriction. An age-restricted room throws `TikTokLiveException` when `throwOnAgeRestriction` is `true`; a room reported offline or not found throws the two exceptions above.
4. Publishes `TikTokPreConnectionEvent` with both responses. A handler that calls `setCancelConnection(true, reason)` aborts the connection with `TikTokLivePreConnectionException`.
5. Downloads the room's gift list when `fetchGifts` is `true`, so `getGiftManager()` knows the gifts before the first `TikTokGiftEvent`.
6. Requests a signed WebSocket URL from the signing provider; a failed or rate-limited request throws `TikTokSignServerException`. The response already carries messages, and they are dispatched to your handlers now, before the socket opens.
7. Starts the WebSocket on its own thread, sets the state to `CONNECTED`, and publishes `TikTokRoomInfoEvent`. `TikTokConnectedEvent` fires from the socket thread once the socket is open, which can be before or after `connect()` returns.

When a step throws a `TikTokLiveException`, `connect()` sets the state back to `DISCONNECTED`, publishes `TikTokErrorEvent` with the exception and then `TikTokDisconnectedEvent` with code `-1` and the reason `Exception: <message>`, and rethrows. `buildAndConnect()` and `connect()` throw it to the caller; `buildAndConnectAsync()` and `connectAsync()` complete their future exceptionally. Any other exception is logged as `Unhandled exception report this bug to github`, the client disconnects, and the stack trace is printed to stderr; nothing is thrown.

With `useEulerstreamWebsocket` set to `true`, `connect()` does none of steps 2 to 7: it publishes `TikTokConnectingEvent`, opens `wss://ws.eulerstream.com` with your `apiKey`, and sets the state to `CONNECTED`. It fills no room information and publishes no `TikTokRoomInfoEvent`; both come from the `roomInfo` message Euler Stream sends after the socket opens, so `getRoomInfo()` is still empty when `TikTokConnectedEvent` fires on that path. See [signing](/configuration/signing/).

## Lifecycle events

Six control events mark the lifecycle. Register them like any other event; every handler receives the `LiveClient` first and the event second.

| Event | Handler | When | Fields |
| --- | --- | --- | --- |
| `TikTokConnectingEvent` | `onConnecting` | First thing `connect()` does | None |
| `TikTokPreConnectionEvent` | `onPreConnection` | After the room data is fetched, before signing | `getUserData()`, `getRoomData()`, `setCancelConnection(boolean, String)`, `getReason()` |
| `TikTokConnectedEvent` | `onConnected` | The WebSocket is open | None |
| `TikTokReconnectingEvent` | `onReconnecting` | Just before a scheduled retry calls `connect()` | None |
| `TikTokDisconnectedEvent` | `onDisconnected` | The socket closed, or a connect attempt failed | `getCode()`, `getReason()`, `isUnknownCloseCode()` |
| `TikTokErrorEvent` | `onError` | An exception in the client, in the socket, or thrown by a handler while a message is processed | `getException()`: a `TikTokLiveMessageException` wrapping the cause when a mapping or handler threw while a message was processed |

`TikTokDisconnectedEvent.getCode()` is the WebSocket close code when TikTok or `disconnect()` closed the socket, and `-1` (`isUnknownCloseCode()` is `true`) when a connect attempt failed; `getReason()` is `None` when the close frame carried no reason. The end of a stream is a message event, not a control event: when TikTok reports the stream ended or suspended, the client publishes `TikTokLiveEndedEvent` (`onLiveEnded`) and does not disconnect by itself. Call `disconnect()` from the handler when the connection should close with the stream.

```java
TikTokLive.newClient("isaackogz")
        .onConnecting((client, event) -> System.out.println("Connecting"))
        .onPreConnection((client, event) -> {
            if (event.getRoomData().isAgeRestricted()) {
                event.setCancelConnection(true, "Age-restricted room");
            }
        })
        .onConnected((client, event) -> System.out.println("Connected to room " + client.getRoomInfo().getRoomId()))
        .onLiveEnded((client, event) -> client.disconnect())
        .onDisconnected((client, event) -> System.out.println("Disconnected: " + event.getCode() + " " + event.getReason()))
        .onError((client, event) -> event.getException().printStackTrace())
        .buildAndConnect();
```

The `onPreConnection` handler and the room id printed by `onConnected` apply to the default path; with `useEulerstreamWebsocket`, the pre-connection event is not published and `getRoomId()` is `null` until the first `TikTokRoomInfoEvent`.

## Disconnect

`disconnect()` is `disconnect(LiveClientStopType.NORMAL)`: it stops the heartbeat, starts the WebSocket close handshake without waiting for it, and sets the state to `DISCONNECTED`. `LiveClientStopType.CLOSE_BLOCKING` waits until the socket has closed, and `LiveClientStopType.DISCONNECT` drops the connection without a close handshake. `TikTokDisconnectedEvent` is published when the socket reports the close. Calling `disconnect()` on a client that is not connected does nothing.

```java
var client = TikTokLive.newClient("isaackogz").buildAndConnect();
Thread.sleep(Duration.ofMinutes(1).toMillis());
client.disconnect(LiveClientStopType.CLOSE_BLOCKING);
```

## Retry when the creator is offline

Two settings control the client's own retry. `retryOnConnectionFailure` (default `false`) makes the client try again when `connect()` fails with `TikTokLiveOfflineHostException`, and `retryConnectionTimeout` (default one second) is the wait before each attempt. The client schedules `connect()` on a daemon thread, publishes `TikTokReconnectingEvent` just before the attempt, and repeats while the creator stays offline.

- Each failed attempt publishes `TikTokErrorEvent` and `TikTokDisconnectedEvent` before scheduling the next.
- The call that started the sequence still throws: catch `TikTokLiveOfflineHostException` around `buildAndConnect()`, or use `buildAndConnectAsync()`.
- The retry thread is a daemon thread, so keep the main thread alive, as the example below does with `System.in.read()`.
- Only an offline creator at connect time is retried. Other failures, and a socket that closes after connecting, are not: handle `onDisconnected`, wait, and call `connect()` again. Waiting matters; a tight loop runs into the signing provider's rate limit.
- The default one-second wait polls TikTok once a second. Set a longer `retryConnectionTimeout` for a creator who may stay offline for hours.

```java
TikTokLive.newClient("isaackogz")
        .configure(settings -> {
            settings.setRetryOnConnectionFailure(true);
            settings.setRetryConnectionTimeout(Duration.ofSeconds(30));
        })
        .onReconnecting((client, event) -> System.out.println("Trying again"))
        .onConnected((client, event) -> System.out.println("Connected"))
        .buildAndConnectAsync();

System.in.read();
```

To reconnect after a drop, poll `getRoomInfo().getConnectionState()` and connect again once the creator is live. `TikTokLive.isLiveOnline` asks TikTok directly and spends no signing request.

```java
var client = TikTokLive.newClient("isaackogz")
        .onComment((liveClient, event) -> System.out.println(event.getText()))
        .build();

while (true) {
    if (client.getRoomInfo().getConnectionState() == ConnectionState.DISCONNECTED
            && TikTokLive.isLiveOnline("isaackogz")) {
        try {
            client.connect();
        } catch (TikTokLiveException e) {
            System.err.println("Connect failed: " + e.getMessage());
        }
    }
    Thread.sleep(Duration.ofSeconds(60).toMillis());
}
```

## Room information

`getRoomInfo()` returns the client's `LiveRoomInfo`. Before connecting it holds `getHostName()`, the state `DISCONNECTED`, and the defaults: `getLanguage()` is `en`, `getUsersRanking()` is an empty list, the counts are `0`, and the other fields are `null`. On the default path, `connect()` fills it from TikTok's responses: `getRoomId()`, `getTitle()`, `getHost()`, `getStartTime()`, `getViewersCount()`, `getTotalViewersCount()`, and `isAgeRestricted()`, all set before `TikTokPreConnectionEvent` is published. With `useEulerstreamWebsocket`, `connect()` leaves it empty; the `roomInfo` message Euler Stream sends after the socket opens copies the room id, title, host, start time, and viewer count in, not the total viewer count or the age restriction (see [known limitations](/troubleshooting/#known-limitations-in-this-release)), and publishes the first `TikTokRoomInfoEvent`. While connected, the same object is updated in place and `TikTokRoomInfoEvent` (`onRoomInfo`) is published with it each time: viewer counts and `getUsersRanking()` from TikTok's room user messages, `getViewersCount()` from member messages too, `getLikesCount()` from like messages, `getLanguage()` and, while it is still `null`, the host from the live intro message. `getConnectionState()` is `CONNECTING`, `CONNECTED`, or `DISCONNECTED`.

```java
TikTokLive.newClient("isaackogz")
        .onRoomInfo((client, event) -> {
            var info = event.getRoomInfo();
            System.out.println(info.getTitle() + ": " + info.getViewersCount() + " viewers, " + info.getLikesCount() + " likes");
        })
        .buildAndConnect();
```

## Check whether a creator is live

`TikTokLive.isLiveOnline(hostName)` asks TikTok whether the creator is live; it uses no signing request and opens no socket, so it is the right call for polling. `TikTokLive.isHostNameValid(hostName)` checks that the username exists. Both have `Async` variants, `isLiveOnlineAsync` and `isHostNameValidAsync`, that return a `CompletableFuture<Boolean>`, and `TikTokLive.requests()` returns the `LiveHttpClient` behind them for the full responses.

```java
if (TikTokLive.isLiveOnline("isaackogz")) {
    TikTokLive.newClient("isaackogz").buildAndConnect();
}
```

## Keep the connection alive

Once the socket is open, the client sends a heartbeat every `pingInterval` milliseconds (default `10000`). `build()` rejects values under `250`. The heartbeat stops with the socket. No setting reconnects after a drop; see the retry section above.
