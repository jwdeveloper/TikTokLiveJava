---
title: Authentication
description: When a TikTok session is needed, how to give the client your sessionId and ttTargetIdc cookies, where the client sends them, how sendChat and throwOnAgeRestriction use them, and the risks.
sidebar:
  order: 4
---

Reading a public livestream needs no TikTok account. The client's session settings are for:

- `sendChat`, which posts as the account the session belongs to.
- Age-restricted rooms. The settings' Javadoc describes a session as the way to receive 18+ content; `throwOnAgeRestriction`, below, decides what `connect()` does with such a room.
- Chat completeness. The `sessionId` Javadoc notes that TikTok does not always send every chat message to a logged-out client, and the signing request forwards `sessionId`; the code comment there says this allows receiving all comments and subscribe events.

## Session cookies

TikTok identifies a logged-in session by the `sessionid` cookie together with `tt-target-idc`, the data center that holds the account. Copy both from your browser's cookies for `tiktok.com` while logged in and set `sessionId` and `ttTargetIdc` in `configure`. Set both: the Javadoc notes that `sessionId` is effective only with a correct `ttTargetIdc`.

```java
TikTokLive.newClient("isaackogz")
        .configure(settings -> {
            settings.setSessionId(System.getenv("TIKTOK_SESSION_ID"));
            settings.setTtTargetIdc(System.getenv("TIKTOK_TARGET_IDC"));
        })
        .onComment((client, event) -> System.out.println(event.getText()))
        .buildAndConnect();
```

`TIKTOK_SESSION_ID` and `TIKTOK_TARGET_IDC` are names this example reads itself; the library reads no environment variables.

## Where the session goes

While the two settings are set, the client sends them with these requests and nowhere else:

| Request | Destination | What is sent |
| --- | --- | --- |
| Creator's user data, `api-live/user/room` | `www.tiktok.com` | `sessionid` and `tt-target-idc` as cookies |
| Room info, `webcast/room/info` | `webcast.tiktok.com` | `sessionid` and `tt-target-idc` as cookies |
| Signing request | The signing provider | `sessionid` as the `session_id` query parameter |
| `sendChat` | The provider's chat endpoint | `sessionId`, `ttTargetIdc`, `roomId`, and `content` as a JSON body |

The gift list request carries no session, the WebSocket handshake sends the cookies from the signing response, and the Euler Stream WebSocket URL carries the `apiKey` only. A `null` value is skipped, so with no session the cookies are not sent at all. See [signing](/configuration/signing/) for the rest of the signing request.

## Sending chat messages

`client.sendChat(content)` posts a message to the connected room with the `sessionId` and `ttTargetIdc` from the settings; `sendChat(content, sessionId, ttTargetIdc)` takes them per call. The request goes to the provider's chat endpoint with the client's `roomId`, so call it after `connect()` has filled the room info, and it carries the `x-api-key` header when `apiKey` is set. The method returns `true` when the provider answers HTTP 200. The interface's Javadoc warns that it is known to return `true` for some session ids although the message was not posted, a TikTok-side behaviour the library cannot detect. In [offline mode](/configuration/client-settings/#offline-mode) it returns `false`.

```java
var client = TikTokLive.newClient("isaackogz")
        .configure(settings -> {
            settings.setSessionId(System.getenv("TIKTOK_SESSION_ID"));
            settings.setTtTargetIdc(System.getenv("TIKTOK_TARGET_IDC"));
        })
        .buildAndConnect();

boolean accepted = client.sendChat("Hello from TikTokLiveJava");
System.out.println(accepted ? "Sent" : "Rejected");
```

## Age-restricted rooms

`throwOnAgeRestriction` is `false` by default. When it is `true`, `connect()` throws `TikTokLiveException` with the message `Livestream for <hostName> is 18+ or age restricted!` as soon as the room info reports the restriction, before `TikTokPreConnectionEvent` and before any signing request. When it is `false`, the connection continues; `getRoomInfo().isAgeRestricted()` and `TikTokPreConnectionEvent.getRoomData().isAgeRestricted()` report the restriction, and a pre-connection handler can cancel the connection itself, as shown on the [connection lifecycle](/getting-started/connection/#lifecycle-events) page.

```java
TikTokLive.newClient("isaackogz")
        .configure(settings -> settings.setThrowOnAgeRestriction(true))
        .onComment((client, event) -> System.out.println(event.getText()))
        .buildAndConnect();
```

## Risks

A TikTok session id grants full access to the account it belongs to.

- It leaves your machine. Every request in the table above sends it, to TikTok and to the signing provider; the provider receives it with each signing request while the setting is set. How the provider handles it is described in [Euler Stream's documentation](https://www.eulerstream.com/docs), not here.
- Keep it out of source control and logs. Read it from the environment or a secret store, as the examples above do, and clear the settings when a client no longer needs them.
- Prefer a secondary account over your own, and do not set a session unless you need one of the three uses above.
