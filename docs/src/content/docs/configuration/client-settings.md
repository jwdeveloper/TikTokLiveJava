---
title: Client settings
description: Every field of LiveClientSettings, its type, default, and effect, how configure applies them, what build validates, and offline mode for tests.
sidebar:
  order: 1
---

`LiveClientSettings` holds the configuration of one client. `TikTokLive.newClient(hostName)` starts from `LiveClientSettings.createDefault()` with `hostName` set, and the builder's `configure(Consumer<LiveClientSettings>)` hands you that object to change. Each `configure` call runs at once and can be repeated; `build()` validates the result and shares the same object with the built client, so set everything before building.

```java
TikTokLive.newClient("isaackogz")
        .configure(settings -> {
            settings.setFetchGifts(true);
            settings.setClientLanguage("de-DE");
            settings.setPingInterval(5000);
        })
        .onComment((client, event) -> System.out.println(event.getText()))
        .buildAndConnect();
```

The fields are Lombok properties: `fetchGifts` is read with `isFetchGifts()` and written with `setFetchGifts(boolean)`, `logLevel` with `getLogLevel()` and `setLogLevel(Level)`. `TikTokLive.requests(Consumer<LiveClientSettings>)` accepts the same settings for a standalone `LiveHttpClient`, the kind of client behind `isLiveOnline`; see [HTTP settings](/configuration/http-settings/).

## Fields

The defaults are the values after `TikTokLive.newClient`, which calls `createDefault()`. One of them, `printToConsole`, differs from the field initialiser, as noted; the other such default, the HTTP `timeout`, is on the [HTTP settings](/configuration/http-settings/) page.

| Field | Type | Default | Effect |
| --- | --- | --- | --- |
| `offline` | `boolean` | `false` | Replaces the HTTP and WebSocket clients with stand-ins that send nothing to TikTok or the signing provider. See [Offline mode](#offline-mode). |
| `fetchGifts` | `boolean` | `false` | When `true`, `connect()` downloads the room's gift list after `TikTokPreConnectionEvent`, so `getGiftManager()` knows every gift before the first `TikTokGiftEvent`. When `false`, the manager starts empty and adds each gift from the first message that carries it. See [gifts](/guides/gifts/). |
| `clientLanguage` | `String` | `"en-US"` | Sent to TikTok as the `app_language` and `webcast_language` request parameters. `build()` replaces `null` or an empty string with `"en"`. |
| `retryOnConnectionFailure` | `boolean` | `false` | Call `connect()` again when the creator is offline. See [connection lifecycle](/getting-started/connection/#retry-when-the-creator-is-offline). |
| `retryConnectionTimeout` | `Duration` | 1 second | The wait before each retry. See [connection lifecycle](/getting-started/connection/#retry-when-the-creator-is-offline). |
| `printToConsole` | `boolean` | `false` | When `false`, the client's logger is set to `Level.OFF`. The field initialiser is `true`; `createDefault()` sets `false`. See [logging](/configuration/logging/). |
| `logLevel` | `java.util.logging.Level` | `Level.ALL` | The level of the client's logger when `printToConsole` is `true`. See [logging](/configuration/logging/). |
| `hostName` | `String` | The `newClient` argument | The creator's username. `build()` removes a leading `@` and throws `TikTokLiveException` when it is `null` or empty. See [proxies and host](/configuration/proxies-and-host/). |
| `httpSettings` | `HttpClientSettings` | TikTok's web-client parameters and headers, a 3-second timeout, no proxy | Parameters, headers, cookies, timeout, proxies, and transport hooks for every HTTP request and the WebSocket handshake. See [HTTP settings](/configuration/http-settings/). |
| `pingInterval` | `long` | `10000` | Milliseconds between heartbeats once the socket is open. `build()` throws `TikTokLiveException` for a value under `250`. Not used on the Euler Stream WebSocket path. |
| `throwOnAgeRestriction` | `boolean` | `false` | When `true`, `connect()` throws `TikTokLiveException` for an age-restricted room instead of connecting. See [authentication](/configuration/authentication/). |
| `useEulerstreamWebsocket` | `boolean` | `false` | Receive events from Euler Stream's WebSocket instead of connecting to TikTok. Needs `apiKey`. See [signing](/configuration/signing/). |
| `useEulerstreamEnterprise` | `boolean` | `false` | Use Euler Stream's enterprise endpoints for signing, chat, and the WebSocket. See [signing](/configuration/signing/). |
| `sessionId` | `String` | `null` | Your TikTok `sessionid` cookie. Sent with TikTok's user and room requests, the signing request, and `sendChat`. See [authentication](/configuration/authentication/). |
| `ttTargetIdc` | `String` | `null` | Your TikTok `tt-target-idc` cookie, needed with `sessionId`. See [authentication](/configuration/authentication/). |
| `roomId` | `String` | `null` | Not read by the client in this checkout: `connect()` always takes the room id from the creator's user data. Setting it has no effect. |
| `apiKey` | `String` | `null` | Your Euler Stream API key. Sent as the `x-api-key` header of the signing and chat requests and in the Euler Stream WebSocket URL. See [signing](/configuration/signing/). |

## What `build()` validates

`build()` runs these checks and adjustments before wiring the client, in this order:

1. An empty or `null` `clientLanguage` becomes `"en"`.
2. An empty or `null` `hostName` throws `TikTokLiveException` with the message `HostName can not be null`; a leading `@` is removed.
3. A `pingInterval` under `250` throws `TikTokLiveException` with the message `Minimum allowed ping interval is 250 milliseconds`.
4. `clientLanguage` is written into the HTTP parameters `app_language` and `webcast_language`.

Nothing else is validated: an `apiKey` or a session is checked only by the server that receives it.

## Offline mode

With `offline` set to `true`, `build()` wires `TikTokLiveHttpOfflineClient` and `TikTokWebSocketOfflineClient` in place of the real clients. No request leaves the process, and `sendChat` returns `false`. Use it to test handlers with `publishEvent`, which dispatches an event object to your handlers, and `publishMessage`, which runs a raw Webcast message through the same mapping as a socket message; both work whether or not the client is connected.

In this checkout, `connect()` in offline mode stops with a logged `NullPointerException` before the stand-in socket starts, because the offline HTTP client returns no room info. `connect()` does not throw; it logs `Unhandled exception`, prints the stack trace, and sets the state back to `DISCONNECTED` without publishing `TikTokConnectedEvent`. The one exception is `useEulerstreamWebsocket` set as well, which skips the HTTP requests, so `connect()` reaches the stand-in socket and succeeds. `build()` and `publishEvent` are enough for a test, so skip `connect()`.

```java
var client = TikTokLive.newClient("isaackogz")
        .configure(settings -> settings.setOffline(true))
        .onComment((liveClient, event) ->
                System.out.println(event.getUser().getProfileName() + ": " + event.getText()))
        .build();

client.publishEvent(TikTokCommentEvent.of("Mark", "Hello world"));
```

The event classes have `of` factories for tests: `TikTokCommentEvent.of(userName, message)`, `TikTokGiftEvent.of(gift)`, `TikTokLikeEvent.of(userName, likes)`, `TikTokFollowEvent.of(userName)`, among others. The repository's `Events_And_Gifts_Testing_Example` also shows `publishMessage` with a Base64 `WebcastLikeMessage` payload.
