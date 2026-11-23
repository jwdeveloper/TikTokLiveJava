---
title: Debugging
description: Turn on the client's log, read every exception through TikTokErrorEvent, watch the raw traffic with the WebSocket debug events, catch messages the library has no mapping for, decode their payloads, and report a new message type.
sidebar:
  order: 7
---

Four tools cover most event debugging: the client's logger, `TikTokErrorEvent` for every exception the client catches, the three WebSocket debug events for the raw traffic, and `publishMessage` for replaying a payload offline. The debug events' fields are on the [debugging events page](/events/debugging/); this guide is about using them. Replace `isaackogz` with a creator who is live.

## Turn on the log

The client is silent until `printToConsole` is set; `logLevel` then chooses what the logger named after the creator lets through, and `client.getLogger()` is the same `java.util.logging.Logger` for your own lines. The client logs its state changes, reconnects, and the warnings of the connect sequence; the handler's level and how to route the log elsewhere are on the [logging page](/configuration/logging/).

```java
TikTokLive.newClient("isaackogz")
        .configure(settings -> {
            settings.setPrintToConsole(true);
            settings.setLogLevel(Level.INFO);
        })
        .onConnected((client, event) -> client.getLogger().info("Connected to room " + client.getRoomInfo().getRoomId()))
        .onDisconnected((client, event) -> client.getLogger().info("Disconnected: " + event.getReason()))
        .buildAndConnect();
```

## Read every exception

The client reports exceptions as `TikTokErrorEvent`, through `onError` or a listener method, with `getException()`. What arrives depends on where it was thrown:

- The connect sequence: `TikTokLiveOfflineHostException`, `TikTokLiveUnknownHostException`, `TikTokSignServerException`, `TikTokLivePreConnectionException`, or a `TikTokLiveException` such as `Already connected`; the event is published and then `connect()` throws the same exception. See [lifecycle events](/getting-started/connection/#lifecycle-events).
- Message handling: when a mapping, or a lambda handler called for a message, throws while a batch is processed, the event carries a `TikTokLiveMessageException` with `messageMethod()`, the message name, `messageToBase64()`, its payload, `webcastResponseToBase64()`, the whole batch, and `getCause()`, the original exception; a mapping that cannot deserialise the payload has as that cause a `TikTokMessageMappingException` when it used the helper's `bytesToWebcastObject`, or protobuf's `InvalidProtocolBufferException` when it called the message class's `parseFrom` directly, as the default handler-based mappings do. The client skips the message and continues with the next one.
- Listener methods: a `TikTokEventListenerMethodException` wrapping the method's exception, from any event; see [listeners](/guides/listeners/#when-a-method-throws).

The message name and payload in a `TikTokLiveMessageException` are what a bug report needs when the library fails on a message it does have a mapping for.

```java
TikTokLive.newClient("isaackogz")
        .onError((client, event) -> {
            if (event.getException() instanceof TikTokLiveMessageException e) {
                System.err.println("Failed on " + e.messageMethod() + ": " + e.getCause());
                System.err.println("Payload: " + e.messageToBase64());
            } else {
                event.getException().printStackTrace();
            }
        })
        .buildAndConnect();
```

## Watch the raw traffic

For every batch of messages, from the signing response and from each WebSocket frame, the client publishes `TikTokWebsocketResponseEvent` with the whole `ProtoMessageFetchResult`, then, per message, either a `TikTokWebsocketMessageEvent` before each event the message maps to, with the message, the event, and the mapping's duration, or a `TikTokWebsocketUnhandledMessageEvent` when no mapping is registered for the message's name. `TikTokHttpResponseEvent` has a handler but no publisher in this release. This handler counts each batch by message name, which shows at a glance what a room sends and how much of it the client maps:

```java
TikTokLive.newClient("isaackogz")
        .onWebsocketResponse((client, event) -> {
            var counts = new TreeMap<String, Integer>();
            for (var message : event.getResponse().getMessagesList()) {
                counts.merge(message.getMethod(), 1, Integer::sum);
            }
            System.out.println("Batch of " + event.getResponse().getMessagesCount() + ": " + counts);
        })
        .onWebsocketMessage((client, event) ->
                System.out.println(event.getMessage().getMethod() + " -> " + event.getEvent().getClass().getSimpleName()
                        + " in " + event.getMetaData().getHandlingTime().toNanos() + " ns"))
        .buildAndConnect();
```

## Messages without a mapping

A message type the library does not know ends up in `TikTokWebsocketUnhandledMessageEvent`, not in `TikTokErrorEvent`. `getMessage()` is the raw message with `getMethod()`, the name, and `getPayload()`, the protobuf bytes. This handler prints each new name once, with its payload in Base64, which is the form a bug report and a `publishMessage` replay both take:

```java
var reported = new HashSet<String>();

TikTokLive.newClient("isaackogz")
        .onWebsocketUnhandledMessage((client, event) -> {
            var method = event.getMessage().getMethod();
            if (!reported.add(method)) {
                return;
            }
            var payload = event.getMessage().getPayload();
            System.out.println("Unhandled " + method + " (" + payload.size() + " bytes): "
                    + Base64.getEncoder().encodeToString(payload.toByteArray()));
        })
        .buildAndConnect();
```

## Decode a payload

Two cases. When the [schema reference](/schema/) has a class for the message, which is so for the sixteen [unmapped events](/events/#unmapped-events), parse the payload with the class's `parseFrom` and, where an event class exists, construct it; the next step is a custom mapping, see [custom events and mappings](/guides/custom-events/). When no class exists, `ProtocolUtils.getProtocolBufferStructure(bytes)` decodes the raw protobuf into a `ProtoBufferObject` whose `toJson()` lists the field numbers, wire types, and values, which is enough to see what the message carries and to write a `.proto` definition; `toProtoFile()` drafts one.

```java
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.api.messages.webcast.WebcastRankTextMessage;

TikTokLive.newClient("isaackogz")
        .onWebsocketUnhandledMessage((client, event) -> {
            var payload = event.getMessage().getPayload();
            if (event.getMessage().getMethod().equals("WebcastRankTextMessage")) {
                try {
                    var rankText = new TikTokRankTextEvent(WebcastRankTextMessage.parseFrom(payload));
                    System.out.println("Rank text: " + rankText.getLabel());
                } catch (InvalidProtocolBufferException e) {
                    System.err.println("Not a WebcastRankTextMessage: " + e.getMessage());
                }
            } else {
                System.out.println(ProtocolUtils.getProtocolBufferStructure(payload.toByteArray()).toJson());
            }
        })
        .buildAndConnect();
```

## Replay a payload offline

A payload captured with the handlers above, or with the [collector](/guides/collector/), can be fed to an offline client with `publishMessage(name, payloadBase64)` and goes through the same mapping as a socket message, so a mapping or a handler can be debugged without a live room, and a failing payload fails right at the `publishMessage` call with the exception. See [offline testing](/guides/offline-testing/#replay-recorded-messages).

## Report a new message type

Check the [schema reference](/schema/) first: when the library already has a class for the message, a custom mapping is the fix and needs no report. When it has none, open an issue on the [TikTokLiveJava GitHub repository](https://github.com/jwdeveloper/TikTokLiveJava/issues) with the message name, one or more Base64 payloads from the unhandled-message handler, the library version (`1.13.1-Release` for these pages), and what was happening in the stream when the message arrived, such as a gift, a battle, or a poll. The payload is what lets the message be decoded and a schema written; a name alone is not enough. For a message the library does map but fails on, report the name and payload from the `TikTokLiveMessageException` instead. The `Unhandled exception` line the client logs when `connect()` hits an unexpected error asks for the same report.
