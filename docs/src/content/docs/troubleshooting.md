---
title: Troubleshooting
description: Diagnose connection failures, signing errors, sockets that close, missing events, and the known limitations of this release, with every exception the library defines and when.
---

Start with the smallest [Getting Started](/) example, the exact dependency version from your build file (`1.13.1-Release` for these pages), the call that failed, and the original exception with its cause chain. A connection has four stages that fail differently: the HTTP requests to TikTok that resolve the room and check that the creator is live, the signing request to [Euler Stream](https://www.eulerstream.com/), the WebSocket handshake with TikTok, and the decoding and mapping of messages once connected. Succeeding at one does not establish that the next works, and a proxy or a session can affect each stage separately.

`LiveClient` has three connection methods, `connect`, `connectAsync` (with or without a consumer), and `disconnect` (with or without a `LiveClientStopType`); two injection methods, `publishEvent` and `publishMessage`; `sendChat`; and four getters, `getRoomInfo`, `getGiftManager`, `getListenersManager`, and `getLogger`. Name the one involved in a report. Every exception the library defines is unchecked and extends `TikTokLiveException`, which extends `RuntimeException`. When a stage of `connect` throws one, the client publishes `TikTokErrorEvent` with it and `TikTokDisconnectedEvent` with the reason `Exception: <message>`, then rethrows it: `buildAndConnect()` and `connect()` throw to the caller; `buildAndConnectAsync()` and `connectAsync()` complete their future exceptionally; `connectAsync(Consumer)` discards the failed future, so its consumer is not called and the two events are the only report. Exceptions raised after the connection is up, on the socket thread or in a handler, reach you only through `onError`; see [read every exception](/guides/debugging/#read-every-exception).

## Exceptions

All eleven classes are in `io.github.jwdeveloper.tiktok.api.exceptions`.

| Exception | Thrown when |
| --- | --- |
| `TikTokLiveException` | The base class, and the one thrown directly for `Already connected`, `HostName can not be null`, `Minimum allowed ping interval is 250 milliseconds`, `Livestream for <host> is 18+ or age restricted!` with `throwOnAgeRestriction`, `Failed to connect to the websocket` when the proxy list is exhausted, `Failed to stop the websocket` when a `CLOSE_BLOCKING` wait is interrupted, `Listener ... has already been registered`, and a `Picture` whose image download fails. `TikTokErrorEvent.of(message)` builds one for tests. |
| `TikTokLiveRequestException` | An HTTP request to TikTok answered with a status other than 200, failed in transport, or returned a body the mapper cannot read: `params_error` from the user request, or a room-information body that is not a JSON object or has no `data` section. Also `No more proxies available!` when the proxy list empties inside one attempt on the `HTTP` or `DIRECT` path; the `SOCKS` path prints that exception and returns a failed result instead. |
| `TikTokSignServerException` | A subclass of the above for the signing request: a status other than 200, with the note described under [signing](/configuration/signing/#rate-limits-and-provider-errors); a 200 without the `x-set-tt-cookie` header; or a body that is not a `ProtoMessageFetchResult`. |
| `TikTokProxyRequestException` | Raised inside `HttpProxyClient` when a request through a proxy endpoint fails with a connect timeout, a `ConnectException`, or another `IOException`; with `autoDiscard` left `true`, its default, the endpoint is removed from the list first for a connect failure, or for any `IOException` on the `SOCKS` path; an `HTTP` endpoint failing with another `IOException` stays in the list. It does not reach your code through the client: the `fetch` methods and `sendChat` catch it and send again while the list is not empty, then send once more, and that last request gets the plain `HttpClient` because the list is empty by then. What you see instead is that direct request's outcome, a result or a `TikTokLiveRequestException` (`TikTokSignServerException` for signing); with `autoDiscard` set to `false`, or with endpoints that are never removed, the loop retries indefinitely while every endpoint keeps failing. It reaches a caller only from a request made directly with `HttpClientBuilder` over settings with proxies enabled, see the [HTTP client page](/advanced/http-client/#how-a-request-is-built). The WebSocket client has one more `throw` of it, for a SOCKS SSL context that cannot be created, which the source marks as unreachable. |
| `TikTokLiveOfflineHostException` | TikTok reported the creator offline, from the user request (`User is offline`) or from the room information (`not found, is the Host offline?`). `getUserData()` holds the user response; `getLiveData()` holds the room response or `null` in the first case. The only failure `retryOnConnectionFailure` retries. |
| `TikTokLiveUnknownHostException` | TikTok reported the creator or the room not found (`User not found`, `could not be found`). Same two getters. Also what a block or captcha page produces, see below. |
| `TikTokLivePreConnectionException` | A `TikTokPreConnectionEvent` handler called `setCancelConnection(true, reason)`; the message is the reason and `getPreconnectEvent()` is the event. |
| `TikTokLiveMessageException` | Any exception thrown while one message of a batch was mapped or handled. Never thrown to you: it is the exception of a `TikTokErrorEvent`, with `messageMethod()`, `messageToBase64()`, `webcastResponseToBase64()`, and the original exception as `getCause()`. |
| `TikTokMessageMappingException` | The mapper helper could not deserialise: the message class has no `parseFrom` method or the bytes do not parse (both read `can't find parsing method`), or a message name has no class in the webcast package. The cause of a `TikTokLiveMessageException` on the socket, and thrown directly by `publishMessage`. |
| `TikTokProtocolBufferException` | A WebSocket frame was not a `WebcastPushFrame`, or its payload not a `ProtoMessageFetchResult`; `getBytes()` holds the bytes. Published as `TikTokErrorEvent` from the socket thread. |
| `TikTokEventListenerMethodException` | Thrown by `addListener` when a `@TikTokEventObserver` method has no parameter that extends `TikTokEvent`, and the exception of a `TikTokErrorEvent` when a listener method throws, wrapping the reflective call's exception; see [when a method throws](/guides/listeners/#when-a-method-throws). |

```java
TikTokLive.newClient("isaackogz")
        .onError((client, event) -> {
            var exception = event.getException();
            if (exception instanceof TikTokLiveMessageException e) {
                System.err.println("Message " + e.messageMethod() + " failed: " + e.getCause());
            } else if (exception instanceof TikTokProtocolBufferException e) {
                System.err.println("Unreadable frame of " + e.getBytes().length + " bytes: " + e.getCause());
            } else if (exception instanceof TikTokEventListenerMethodException e) {
                System.err.println("A listener method failed: " + e.getCause());
            } else {
                exception.printStackTrace();
            }
        })
        .buildAndConnect();
```

## connect() fails

```java
try {
    TikTokLive.newClient("isaackogz").buildAndConnect();
} catch (TikTokLiveOfflineHostException e) {
    System.err.println("Not live: " + e.getMessage());
} catch (TikTokLiveUnknownHostException e) {
    System.err.println("No such creator: " + e.getMessage());
} catch (TikTokSignServerException e) {
    System.err.println("Signing failed: " + e.getMessage());
} catch (TikTokLiveException e) {
    System.err.println("Connect failed: " + e.getMessage());
}
```

**`User is offline` or `not found, is the Host offline?`** (`TikTokLiveOfflineHostException`): the creator is not live. Poll with `TikTokLive.isLiveOnline(hostName)`, which asks TikTok directly and spends no signing request, and connect when it returns `true`; or set `retryOnConnectionFailure` with a `retryConnectionTimeout` of more than the default one second. See [retry when the creator is offline](/getting-started/connection/#retry-when-the-creator-is-offline).

**`User not found`** (`TikTokLiveUnknownHostException`): the username does not exist, or TikTok did not answer with the JSON the client expects. `hostName` is the username after the `@` in the profile URL, not the display name; a leading `@` is removed. When the name is right, turn on the log: the client logs `Malformed Json` at `WARNING` with the body it received, and returns the same `NotFound` status for a captcha or block page as for a missing user. Try again later, change the address the requests come from, or go through a [proxy](/configuration/proxies-and-host/).

**`is 18+ or age restricted!`** (`TikTokLiveException`): the room is age-restricted and `throwOnAgeRestriction` is set. Without the setting the client connects; whether it receives the room's messages depends on the session, see [age-restricted rooms](/configuration/authentication/#age-restricted-rooms).

**`Unable to get websocket connection credentials`** (`TikTokSignServerException`): the signing request failed. The message carries the status: `429` is the provider's rate limit, with the wait when the provider sent a `ratelimit-reset` header; `420`, `500` to `504`, and other statuses are described on the [signing page](/configuration/signing/#rate-limits-and-provider-errors). Each connection attempt spends one signing request, so poll with `isLiveOnline` rather than by connecting, and set an `apiKey` for higher limits. The client does not retry a signing failure without proxies.

**`Already connected`** (`TikTokLiveException`): `connect` was called while the state is `CONNECTING` or `CONNECTED`. The failure branch still runs: the state flips to `DISCONNECTED` and `TikTokErrorEvent` and `TikTokDisconnectedEvent` are published while the existing socket stays open, and the next `connect` replaces that socket. Use one connection per client, or `disconnect` first; see [build, connect, or both](/getting-started/connection/#build-connect-or-both).

**`Unhandled exception report this bug to github`** in the log: `connect` hit an exception that is not a `TikTokLiveException`. The client logs that line, calls `disconnect`, prints the stack trace to stderr, and returns normally with the state `DISCONNECTED`, without publishing `TikTokDisconnectedEvent` unless a socket was open. In this release the known cause is `connect` on an offline client, which fails with `NullPointerException` at `TikTokLiveClient.java:136`; see [known limitations](#known-limitations-in-this-release). Anything else is worth a report.

**Nothing happens after `buildAndConnectAsync()`**: the connection runs on the common pool, whose threads are daemon threads, and the socket's read thread inherits that flag, so a `main` that returns ends the program whether or not the socket has opened. Keep the main thread alive for as long as the connection should live, as the repository's `ConnectionExample` does with `System.in.read()`.

## The socket closes

`TikTokDisconnectedEvent.getCode()` says what happened. `-1` (`isUnknownCloseCode()`) means the connection never opened: either a stage of `connect` threw, with the reason `Exception: <message>`, or the WebSocket handshake failed on the socket thread, in which case a `TikTokErrorEvent` with the handshake exception precedes it and `connect` returned normally. `1000` is a normal close from either side, including your own `disconnect`; `1006` means the connection dropped without a close frame; `1002` means TikTok answered the handshake with an HTTP error rather than the upgrade. See the [WebSocket page](/advanced/websocket/#how-a-connection-ends).

The client does not reconnect after a close. Handle `onDisconnected`, wait, check `isLiveOnline`, and call `connect` again; the [connection page](/getting-started/connection/#retry-when-the-creator-is-offline) has the loop. A tight loop runs into the signing provider's rate limit.

A handshake that hangs rather than fails is consistent with the connect timeout the socket receives: `timeout.toMillisPart()`, which is `0`, no timeout, for whole-second values such as the default three seconds. Set `Duration.ofMillis(2500)` or similar to give the handshake a timeout; see [the timeout and sub-second parts](/configuration/http-settings/#the-timeout-and-sub-second-parts). With proxies enabled, the socket also goes through the list unless `allowWebsocket` is `false`; a proxy that passes HTTP but not WebSocket traffic shows up here, see [the WebSocket through proxies](/configuration/proxies-and-host/#the-websocket).

`TikTokLiveEndedEvent` does not close the socket: when TikTok reports the stream ended or suspended, the client publishes the event and keeps the connection until you call `disconnect` or TikTok closes it. Call `disconnect` from `onLiveEnded` when the connection should end with the stream.

## Connected, but no expected events

1. Register handlers and listeners before `connect`. The signing response already carries messages, and they are dispatched before the socket opens; a handler added afterwards misses them.
2. Dispatch is by exact class. A handler receives the events of its own class and, for `onEvent(TikTokEvent.class, ...)`, every event; it does not receive subclasses. `onGift` never sees `TikTokGiftComboEvent`, and `onEvent(TikTokLinkEvent.class, ...)` sees only the generic link event, not `TikTokLinkInviteEvent` and the other link subclasses. Subscribe to each class you need. The order in which several handlers for one class run is unspecified.
3. Sixteen event classes are never produced by the default client because their mappings are not registered; the [unmapped events](/events/#unmapped-events) list names them and shows the custom mapping that enables one. `TikTokHttpResponseEvent` has a handler but no publisher in this release.
4. Listen for `TikTokWebsocketUnhandledMessageEvent` to see every message the client has no mapping for, with its name and payload, and for `TikTokWebsocketResponseEvent` to see every batch. The [debugging guide](/guides/debugging/) shows both.
5. The setting's Javadoc and a comment in the HTTP client say that without a `sessionId` TikTok does not send every chat message; the client cannot verify that, but a session is what the maintainers recommend for complete comments, see [authentication](/configuration/authentication/).
6. With `useEulerstreamWebsocket`, `getRoomInfo()` is empty until the provider's `roomInfo` message arrives, and no `TikTokPreConnectionEvent` is published; see [signing](/configuration/signing/#euler-stream-websocket).
7. Confirm that the room produces the feature. A message defined in the schema is not sent in every room or region; see [protocol](/advanced/protocol/#schema-definitions-and-verified-behaviour).

## Known limitations in this release

These describe the checked-in `1.13.1-Release` code; recheck them against a newer release.

- `connect` on a client with `offline` set fails with `NullPointerException` at `TikTokLiveClient.java:136`, because the offline HTTP client returns a user response with no room info; it is logged, not thrown. With `useEulerstreamWebsocket` also set, `connect` makes no HTTP request and succeeds against the stand-in socket. Use `build()` with `publishEvent` and `publishMessage` instead, see [offline testing](/guides/offline-testing/).
- With proxies of type `HTTP` or `DIRECT`, a response other than 200 moves to the next endpoint without removing the current one, and the loop runs while the list is not empty, so a provider answering 429 through working proxies is retried in rotation indefinitely. See [how a request uses the list](/configuration/proxies-and-host/#how-a-request-uses-the-list).
- The `roomId` setting on `LiveClientSettings` is never read; the room id always comes from the user request.
- The WebSocket handshake and the SOCKS proxy path receive `timeout.toMillisPart()` as their connect timeout, so a whole-second `timeout` gives them none.
- There is no name-based fallback mapping: `TikTokGenericEventMapper.mapToEvent` exists but nothing calls it, so the sixteen unmapped events stay unmapped until you register them.
- Event dispatch is by exact class; subclasses do not reach a superclass handler.
- `TikTokHttpResponseEvent` is never published by the HTTP client.
- On the Euler Stream path the `roomInfo` message fills only the room id, title, start time, viewer count, and host; the total viewer count and the age restriction stay unset, and the message's `totalViewers` is written into the viewer count.
- `stop` on the Euler Stream socket closes the socket only when it is already open; one still in its handshake is dropped without being closed.
- The recorder's `setOutputFile` needs a path with a directory component; a bare filename such as `new File("clip.flv")` fails the download task with a `NullPointerException` that is printed and swallowed, and no ended event is published. See the [recorder settings](/guides/recorder/#settings).
- A second `connect` while connected flips the state to `DISCONNECTED` and publishes error and disconnected events while the socket stays open.
- The status notes in request failures name the sign server even for TikTok's own API: a 429 from `www.tiktok.com` reads `Sign server rate limit reached`.
- The poll mapping and `TikTokShopEvent` carry a `not working` note in the source; see [room and display](/events/room-and-display/) and [games and commerce](/events/games-and-commerce/).

## Report a reproducible issue

File reports on the [TikTokLiveJava GitHub issues](https://github.com/jwdeveloper/TikTokLiveJava/issues) page. Include the library version, the Java version and operating system, the settings with secrets removed, a minimal example, the exact exception with its cause chain, and which stage failed: user data, room information, signing, WebSocket handshake, or message handling. For an event that does not arrive or a message that fails to map, add the message `method` and a Base64 payload, from `TikTokWebsocketUnhandledMessageEvent` or from `TikTokLiveMessageException.messageToBase64()`, reviewed for personal data; see [report a new message type](/guides/debugging/#report-a-new-message-type). Never include session cookies, API keys, or signed WebSocket URLs.
