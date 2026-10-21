---
title: WebSocket layer
description: The socket clients behind connect(), how the signed URL becomes a connection, the push frames, acknowledgements, and heartbeats, how a connection ends, the Euler Stream and offline sockets, and reading the frames without the client.
sidebar:
  order: 2
---

`LiveSocketClient` is the interface `TikTokLiveClient` drives: `start(connectionData, liveClient)` opens the connection, `stop(LiveClientStopType)` closes it, and `isConnected()` reports whether the socket is open. `build()` picks the implementation from the settings: `TikTokWebSocketClient` by default, `TikTokWebSocketEulerClient` with `useEulerstreamWebsocket`, and `TikTokWebSocketOfflineClient` with `offline`. The sockets are [Java-WebSocket](https://github.com/TooTallNate/Java-WebSocket) 1.5.5 clients, a compile-scope dependency of the `Client` module.

## Connection sequence

`TikTokWebSocketClient.start` runs on the thread that called `connect()`, after the signing request to [Euler Stream](https://www.eulerstream.com/) has returned a `LiveConnectionData.Response`:

1. `stop(LiveClientStopType.NORMAL)` closes any socket the client still holds, so a second `start` replaces the first.
2. The messages already in the signing response are handed to the message handler, so handlers registered before `connect()` receive them before the socket exists.
3. A `TikTokWebSocketListener` is created for the signed URL with the HTTP settings' headers plus a `Cookie` header holding the cookies from the signing response, and the `timeout` of the HTTP settings as its connect timeout, after `toMillisPart()`: a whole-second timeout, such as the default, gives `0`, which is no timeout. See [the timeout and sub-second parts](/configuration/http-settings/#the-timeout-and-sub-second-parts).
4. With proxies enabled and `allowWebsocket` left `true`, the listener connects through the proxy list, as the [proxies page](/configuration/proxies-and-host/#the-websocket) describes; otherwise it connects directly. Java-WebSocket's `connect()` starts a thread named `WebSocketConnectReadThread-<id>` and returns at once. The library sets no daemon flag on it, so the thread inherits the flag of the thread that called `connect()`: a user thread when you call `connect()` or `buildAndConnect()` from your main thread, which then keeps the process alive while the socket is open; a daemon thread when you use `connectAsync()` or `buildAndConnectAsync()`, which run `connect()` on the common pool, so the process ends with your last non-daemon thread unless you keep one alive.
5. `connect()` sets the state to `CONNECTED` and publishes `TikTokRoomInfoEvent` without waiting for the handshake. `TikTokConnectedEvent` is published from the socket thread when the handshake completes.

A handshake that fails is reported on the socket thread rather than thrown from `connect()`. When the host does not resolve or the TCP or TLS connection fails, the listener's `onError` publishes `TikTokErrorEvent` with the exception, then its `onClose` runs with code `-1`, the client's state goes to `DISCONNECTED`, and `TikTokDisconnectedEvent` is published with that code, for which `isUnknownCloseCode()` is `true`. When TikTok answers the upgrade request with an HTTP error instead, Java-WebSocket closes with its protocol-error code `1002` through the same `onClose` path. `connect()` has already returned normally in both cases.

```java
TikTokLive.newClient("isaackogz")
        .onError((client, event) -> System.err.println("Socket error: " + event.getException()))
        .onDisconnected((client, event) -> {
            if (event.isUnknownCloseCode()) {
                System.err.println("The connection never opened or the connect attempt failed: " + event.getReason());
            } else {
                System.out.println("Closed with code " + event.getCode());
            }
        })
        .buildAndConnect();
```

## Frames

Every binary message from TikTok is a [`WebcastPushFrame`](/schema/webcast/webcastpushframe/), whose `Payload` is a [`ProtoMessageFetchResult`](/schema/webcast/protomessagefetchresult/), the same batch type the signing response carries. The listener parses the frame, drops frames with an empty payload, parses the batch, and hands it to the message handler. A frame or payload that does not parse throws `TikTokProtocolBufferException`, which carries the bytes in `getBytes()`; `onMessage` catches it and every other exception from handling and publishes `TikTokErrorEvent`. There is no decompression step: frames are parsed as received.

When a batch has `needsAck` set, the listener sends an acknowledgement before handling it: a `WebcastPushFrame` with `PayloadType` `ack`, the incoming frame's `LogId`, and the batch's `internalExt` as the payload. After every message, after the handshake, and after an error the listener also sends a WebSocket ping frame through Java-WebSocket's `sendPing()`, when the socket is open. Text frames are ignored on the TikTok socket.

## Heartbeats

`WebSocketHeartbeatTask` sends the heartbeat. `onOpen` starts it with `pingInterval` from the client settings, `10000` milliseconds by default and at least `250`: a task on the daemon `heartbeat-pool` scheduler sends a fixed frame at once and then at that interval while the socket is open. The frame is the eight bytes `MgJwYjoCaGI=` in Base64, a `WebcastPushFrame` with `PayloadEncoding` `pb` and `PayloadType` `hb` and no payload. The task stops when the socket is no longer open, when `stop` is called, and when the socket closes; `start` replaces it with the new socket's task. The Euler Stream socket does not run it.

## How a connection ends

`stop(type)` cancels the heartbeat and detaches the socket from the client before closing it, so the close that follows is not treated as the current socket's. A socket whose handshake has not completed is closed with code `1000` without waiting. An open socket is closed according to the type: `NORMAL` (what `disconnect()` uses) starts the close handshake and returns; `CLOSE_BLOCKING` waits for it, and throws `TikTokLiveException` with the message `Failed to stop the websocket` if the wait is interrupted; `DISCONNECT` drops the connection with no handshake. The types are on the [connection page](/getting-started/connection/#disconnect).

When the socket reports its close, the listener's `onClose` checks the socket against the client's. If it is still the client's, the client's `disconnect()` runs, which sets the state to `DISCONNECTED`, and `TikTokDisconnectedEvent` is published with the close code and reason. If `stop` had already detached it, your own `disconnect()` for instance, the event is published without a second `disconnect()`. A socket replaced by a newer `start` publishes nothing. The code is `1000` after a normal close from either side, `1006` when the connection dropped without a close frame, `1002` when TikTok rejected the handshake, and `-1` when the connection never opened. The client does not reconnect on its own; the [retry section](/getting-started/connection/#retry-when-the-creator-is-offline) shows a loop that does. `TikTokLiveEndedEvent` is a message, not a close: the socket stays open until you call `disconnect()` or TikTok closes it, see [stream control](/events/stream-control/).

## The Euler Stream socket

With `useEulerstreamWebsocket`, `TikTokWebSocketEulerClient.start` ignores the connection data, which is `null` on that path, and opens `wss://ws.eulerstream.com?uniqueId=<hostName>&apiKey=<apiKey>&features.rawMessages=true`, plus `&features.useEnterpriseApi=true` with `useEulerstreamEnterprise`, with the HTTP settings' headers and the same `toMillisPart()` timeout. It uses no proxy and no heartbeat task. The listener is a `TikTokWebSocketEulerListener`, which inherits the binary-frame handling above and adds text frames: a JSON object whose `messages` array holds a `workerInfo` message, logged at `INFO`, and a `roomInfo` message, mapped with `LiveUserDataMapper.mapEulerstream`, copied into `getRoomInfo()`, and published as the first `TikTokRoomInfoEvent`. A text frame that is not a JSON object, or that does not parse, is reported as `TikTokErrorEvent`; a JSON object without a `messages` array, or a message of another type, is ignored. These two messages arrive after the handshake, so `getRoomInfo()` is empty when `TikTokConnectedEvent` fires on this path; the fields the message fills are listed on the [HTTP client page](/advanced/http-client/#response-mappers).

`stop` on this client closes the socket only when it is already open; a socket still in its handshake is dropped from the client without being closed. The Euler path is described from the settings side on the [signing page](/configuration/signing/#euler-stream-websocket).

## The offline socket

With `offline`, `TikTokWebSocketOfflineClient.start` publishes `TikTokConnectedEvent` at once and `stop` publishes `TikTokDisconnectedEvent` with the reason `Stopping` and code `-1`, but only once `start` has given it the client; `isConnected()` is always `false`. In this release `connect()` fails before `start` is reached, see [offline mode](/configuration/client-settings/#offline-mode), so `stop` has no client and `disconnect()` publishes nothing: on an offline client these two events come only from your own `publishEvent`. The exception is `offline` together with `useEulerstreamWebsocket`: that path makes no HTTP request, so `start` runs, `connect()` succeeds, and `disconnect()` publishes the `Stopping` event. The [offline testing guide](/guides/offline-testing/) covers what the mode is for.

## Read the frames without the client

The pieces are public, so a program can fetch the connection data with `TikTokLive.requests()` and open the signed URL with a Java-WebSocket client of its own, which shows the raw batches without any mapping. This reader prints the method name of every message, first from the signing response and then from each frame:

```java
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.api.messages.webcast.ProtoMessageFetchResult;
import io.github.jwdeveloper.tiktok.api.messages.webcast.WebcastPushFrame;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.nio.ByteBuffer;

var requests = TikTokLive.requests();
var userData = requests.fetchLiveUserData("isaackogz");
var connection = requests.fetchLiveConnectionData(userData.getRoomInfo().getRoomId());

for (var message : connection.getWebcastResponse().getMessagesList()) {
    System.out.println("initial " + message.getMethod());
}

var headers = new HashMap<>(LiveClientSettings.DefaultRequestHeaders());
headers.put("Cookie", connection.getWebsocketCookies());

var socket = new WebSocketClient(connection.getWebsocketUrl(), headers) {
    @Override
    public void onOpen(ServerHandshake handshake) {
        System.out.println("Open");
    }

    @Override
    public void onMessage(String text) {
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        try {
            var frame = WebcastPushFrame.parseFrom(bytes.array());
            var batch = ProtoMessageFetchResult.parseFrom(frame.getPayload());
            for (var message : batch.getMessagesList()) {
                System.out.println(message.getMethod());
            }
        } catch (InvalidProtocolBufferException e) {
            System.err.println("Not a push frame: " + e.getMessage());
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        System.out.println("Closed " + code + " " + reason);
    }

    @Override
    public void onError(Exception e) {
        e.printStackTrace();
    }
};

socket.connect();
```

`fetchLiveUserData` throws for an unknown creator and returns `NotFound` with a `null` room info for a malformed answer, so check `getUserStatus()` before reading the room id in anything but a sketch. The reader sends neither the `ack` frames nor the heartbeat the client sends; add both for anything beyond a look at the traffic. What the batches contain, and how a message becomes an event, is on the [protocol page](/advanced/protocol/).
