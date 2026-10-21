---
title: Signing
description: How TikTokLiveJava obtains its WebSocket connection from Euler Stream, what the apiKey setting does, how rate limits and provider errors surface, and the useEulerstreamWebsocket and useEulerstreamEnterprise paths.
sidebar:
  order: 3
---

TikTokLiveJava does not build the Webcast WebSocket URL itself. During `connect()`, after the room data is fetched, it asks a signing provider for the first fetch result and the signed WebSocket URL. The provider is [Euler Stream](https://www.eulerstream.com/), reached at `https://tiktok.eulerstream.com/webcast/fetch`; the URL is a constant of `TikTokLiveHttpClient`, and there is no setting for another provider. Signing works without an account; an API key raises the limits that apply to you. Plans, limits, and endpoint permissions are set by the provider and described in [Euler Stream's documentation](https://www.eulerstream.com/docs), not here.

## What is sent to the provider

Each connection attempt makes a GET request to the provider. It carries the default [HTTP settings](/configuration/http-settings/) parameters and headers, including the `User-Agent`, plus:

- `client=ttlive-java` and `room_id` as query parameters.
- `session_id`, only when `sessionId` is set; see [authentication](/configuration/authentication/).
- The `x-api-key` header, only when `apiKey` is set.

The response's `x-set-tt-cookie` header holds the cookies the WebSocket handshake sends to TikTok. Its body is a `ProtoMessageFetchResult`: the client builds the WebSocket URL from its push server, cursor, internal ext, and route parameters, and dispatches the messages it already contains to your handlers before the socket opens.

The requests to TikTok's own API, the creator's user data at `www.tiktok.com`, and the room info and the gift list at `webcast.tiktok.com`, are not signed and do not touch the provider. `sendChat` is the other request that does: it posts to the provider's chat endpoint, `https://tiktok.eulerstream.com/webcast/chat`.

## API key

Set `apiKey` in `configure`. The client sends it as the `x-api-key` header of the signing and chat requests and as the `apiKey` query parameter of the Euler Stream WebSocket URL. The library reads no environment variable itself; the example reads one and passes it on.

```java
TikTokLive.newClient("isaackogz")
        .configure(settings -> settings.setApiKey(System.getenv("EULER_API_KEY")))
        .onComment((client, event) -> System.out.println(event.getText()))
        .buildAndConnect();
```

## Rate limits and provider errors

The signing request must answer HTTP 200. Any other status makes `connect()` throw `TikTokSignServerException`, whose message starts with `Unable to get websocket connection credentials` and carries the status code and one of these notes:

| Status | Note |
| --- | --- |
| `429` | `Sign server rate limit reached. Try again in mm:ss.`, computed from the `ratelimit-reset` header in seconds, or `Try again later.` when the header is missing. |
| `420` | `IP Cloudflare Blocked.` |
| `500` to `503` | `Sign server Error. Try again later.` |
| `504` | `Sign server Timeout. Try again later.` |
| Any other | The status code alone. |

`TikTokSignServerException` is also thrown when a 200 response has no `x-set-tt-cookie` header, after the client logs the response headers at `WARNING`, and when its body is not a valid `ProtoMessageFetchResult`. Without proxies, the client does not retry a failed signing request; `retryOnConnectionFailure` covers an offline creator only, see [connection lifecycle](/getting-started/connection/#retry-when-the-creator-is-offline). Each connection attempt spends one signing request, so poll with `TikTokLive.isLiveOnline`, which asks TikTok directly, rather than by connecting.

```java
try {
    TikTokLive.newClient("isaackogz").buildAndConnect();
} catch (TikTokSignServerException e) {
    System.err.println("Signing failed: " + e.getMessage());
}
```

With proxies enabled, the signing request is repeated through the proxy list on `TikTokProxyRequestException` and `TikTokSignServerException` while endpoints remain, then once without a proxy; see [proxies and host](/configuration/proxies-and-host/).

## Euler Stream WebSocket

With `useEulerstreamWebsocket` set to `true`, the client receives its events from the managed [TikTok LIVE WebSocket API](https://www.eulerstream.com/websockets) instead of connecting to TikTok. `connect()` publishes `TikTokConnectingEvent`, opens `wss://ws.eulerstream.com` with `uniqueId` set to `hostName`, `apiKey` set to your key, and `features.rawMessages=true`, sends the HTTP settings' headers in the handshake, and sets the state to `CONNECTED`. On this path the client makes no request to TikTok and no signing request, publishes no `TikTokPreConnectionEvent`, downloads no gift list, starts no `pingInterval` heartbeat (the socket listener answers each message with a ping instead), and ignores the proxy settings. An `apiKey` is required; without one the URL carries `apiKey=null`.

The first two messages are JSON: `workerInfo`, which the client logs at `INFO`, and `roomInfo`, which fills `getRoomInfo()` and publishes the first `TikTokRoomInfoEvent`. Binary frames go through the same message handler as on the TikTok socket, so your handlers receive the same events. See [connection lifecycle](/getting-started/connection/#the-connect-sequence) for what differs in the lifecycle.

```java
TikTokLive.newClient("isaackogz")
        .configure(settings -> {
            settings.setApiKey(System.getenv("EULER_API_KEY"));
            settings.setUseEulerstreamWebsocket(true);
        })
        .onRoomInfo((client, event) -> System.out.println("Room " + event.getRoomInfo().getRoomId()))
        .onComment((client, event) -> System.out.println(event.getText()))
        .buildAndConnect();
```

## Enterprise endpoints

With `useEulerstreamEnterprise` set to `true`, the signing request goes to `https://tiktok.enterprise.eulerstream.com/webcast/fetch`, `sendChat` posts to `https://tiktok.enterprise.eulerstream.com/webcast/chat`, and the Euler Stream WebSocket URL gains `features.useEnterpriseApi=true`. The setting's Javadoc notes that it requires an API key; what that key must be entitled to is decided by the provider.
