---
title: Stream control
description: The three events TikTok's control message becomes, live ended, paused, and unpaused, and what the client does when the stream ends.
sidebar:
  order: 10
---

TikTok reports the stream's state in [`WebcastControlMessage`](/schema/webcast/webcastcontrolmessage/), and the client maps its [`ControlAction`](/schema/enums/controlaction/) to one of three events, or to `TikTokUnhandledControlEvent` on [moderation and access](/events/moderation-and-access/#tiktokunhandledcontrolevent) for any other action. None of the three carries a field; they extend `TikTokEvent` directly and have no message header. They are message events, not control events: the client's own connection events are on [control events](/events/control/).

Each event has a builder method, named below. Replace `isaackogz` with a creator who is live.

## TikTokLiveEndedEvent

Triggered when the live stream gets terminated by the host, or suspended by TikTok. Handler: `onLiveEnded`. Message: [`WebcastControlMessage`](/schema/webcast/webcastcontrolmessage/) with the action `STREAM_ENDED` or `STREAM_SUSPENDED`.

The client does not disconnect by itself when the stream ends; `TikTokDisconnectedEvent` follows only when the socket closes. Call `disconnect()` from the handler when the connection should close with the stream.

```java
TikTokLive.newClient("isaackogz")
        .onLivePaused((client, event) -> System.out.println("Paused"))
        .onLiveUnpaused((client, event) -> System.out.println("Resumed"))
        .onLiveEnded((client, event) -> {
            System.out.println("Stream ended");
            client.disconnect();
        })
        .buildAndConnect();
```

## TikTokLivePausedEvent

The host paused the stream. Handler: `onLivePaused`. Message: [`WebcastControlMessage`](/schema/webcast/webcastcontrolmessage/) with the action `STREAM_PAUSED`. The connection stays open while the stream is paused.

## TikTokLiveUnpausedEvent

The host resumed a paused stream. Handler: `onLiveUnpaused`. Message: [`WebcastControlMessage`](/schema/webcast/webcastcontrolmessage/) with the action `STREAM_UNPAUSED`.
