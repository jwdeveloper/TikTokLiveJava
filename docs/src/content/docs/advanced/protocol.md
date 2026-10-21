---
title: Protocol
description: The Webcast protobuf schema TikTokLiveJava is built from, the transport messages, how a message name becomes an event class, how to decode and replay a payload, and the difference between a schema definition and verified behaviour.
sidebar:
  order: 3
---

TikTok's Webcast protocol is not documented by TikTok. TikTokLiveJava carries its own description of it as three proto3 files in `API/src/main/proto/`, and the `API` module's Maven build generates the Java classes from them with `protoc` (`protoc-jar-maven-plugin`, protoc 4.26.1), so the classes are in `API/target/generated-sources` rather than in `src/`. The [schema reference](/schema/) is generated from the same files on every documentation build.

| File | Contents | Java package |
| --- | --- | --- |
| `webcast.proto` | 39 top-level messages: the transport messages below and the `Webcast*` messages that become events; imports the other two files | `io.github.jwdeveloper.tiktok.api.messages.webcast` |
| `data.proto` | 100 messages for the nested data: `User`, `CommonMessageData`, `GiftStruct`, the battle, link, and badge structures | `io.github.jwdeveloper.tiktok.api.messages.data` |
| `enums.proto` | 94 enums | `io.github.jwdeveloper.tiktok.api.messages.enums` |

Each file sets `java_multiple_files`, so every message is its own class, and nested messages are inner classes: `ProtoMessageFetchResult.BaseProtoMessage`, `WebcastBarrageMessage.BarrageType`.

## Transport messages

Three messages carry everything else. The [WebSocket page](/advanced/websocket/#frames) describes when each is sent and received.

| Message | Fields | Role |
| --- | --- | --- |
| [`WebcastPushFrame`](/schema/webcast/webcastpushframe/) | `SeqId`, `LogId`, `Service`, `Method`, `headers`, `PayloadEncoding`, `PayloadType`, `Payload` | One WebSocket message in either direction. Incoming frames carry a batch in `Payload`; the client sends `ack` frames and `hb` heartbeats as frames with no batch. |
| [`ProtoMessageFetchResult`](/schema/webcast/protomessagefetchresult/) | `messages`, `cursor`, `fetchInterval`, `now`, `internalExt`, `fetchType`, `routeParamsMap`, `heartBeatDuration`, `needsAck`, `pushServer`, `isFirst`, `historyCommentCursor`, `historyNoMore` | A batch of messages: the body of the signing response from [Euler Stream](https://www.eulerstream.com/), whose `pushServer`, `cursor`, `internalExt`, and `routeParamsMap` build the WebSocket URL, and the payload of every incoming frame. `TikTokWebsocketResponseEvent.getResponse()` is one of these. |
| [`BaseProtoMessage`](/schema/webcast/protomessagefetchresult-baseprotomessage/) | `method`, `payload`, `msgId`, `msgType`, `offset`, `isHistory` | One message in a batch: the name of its type and its serialized bytes. `TikTokWebsocketMessageEvent.getMessage()` and `TikTokWebsocketUnhandledMessageEvent.getMessage()` are these. |

## From a message to an event

`method` is the name of a message type, `WebcastGiftMessage` for example, and `payload` is that message serialized. The name matches the generated class in the webcast package, which is how `LiveMapperHelper.bytesToWebcastObject(bytes, name)` finds a class for a name and how `isMessageHasProtoClass` answers. The mappings `MessagesMapperFactory` registers are keyed by the same names; a message whose name has no mapping is published as `TikTokWebsocketUnhandledMessageEvent` with the raw `BaseProtoMessage`, which is where a type the library does not know, or one of the sixteen [unmapped events](/events/#unmapped-events), ends up. The [HTTP client and mappers page](/advanced/http-client/#the-message-mappers) describes the pipeline.

An event class wraps its message: `TikTokGiftEvent` is constructed from a `WebcastGiftMessage`, `TikTokCommentEvent` from a `WebcastChatMessage`, and so on, and the `Webcast*` messages that become events share a `CommonMessageData common = 1` field, from which `TikTokHeaderEvent` takes `getMessageId()` (`msgId`), `getRoomId()`, and `getTimeStamp()` (`createTime`). The event's other getters read the message's fields and convert the nested protobuf objects, such as the `User` and `GiftStruct` messages, into the library's own `User` and `Gift` models. The [event catalog](/events/) names the message behind each event and links its schema page; the generated [API reference](/api/) has every event's getters.

## Decode a message

The generated classes have the standard protobuf-java API: `parseFrom(byte[])` and `parseFrom(ByteString)` decode, `toByteArray()` encodes, and `newBuilder()` builds a message by hand. An event class takes the decoded message, so a payload can be turned into the same event the client would publish. For bytes whose type the library has no class for, `ProtocolUtils.getProtocolBufferStructure(bytes)` decodes the wire format without a schema into a `ProtoBufferObject`, whose `toJson()` lists field numbers, wire types, and values and whose `toProtoFile()` drafts a `.proto` definition.

```java
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.api.messages.webcast.WebcastChatMessage;

var message = WebcastChatMessage.newBuilder().setContent("hello").build();
byte[] payload = message.toByteArray();

try {
    var decoded = WebcastChatMessage.parseFrom(payload);
    var event = new TikTokCommentEvent(decoded);
    System.out.println(event.getText() + " from user id " + event.getUser().getId());
    System.out.println(ProtocolUtils.getProtocolBufferStructure(payload).toJson());
} catch (InvalidProtocolBufferException e) {
    System.err.println("Not a WebcastChatMessage: " + e.getMessage());
}
```

A payload captured from a live session is handled the same way. `ProtocolUtils.toBase64(bytes)` and `fromBase64(text)` convert payloads for a report or a fixture, `toBase64(BaseProtoMessage)` encodes a whole message with its name, and `fromBase64ToMessage(text)` reads one back. `publishMessage(name, payloadBase64)` on any client, including an offline one, runs a payload through the mapping as if it had arrived on the socket; see [replay recorded messages](/guides/offline-testing/#replay-recorded-messages) and the [debugging guide](/guides/debugging/#decode-a-payload) for capturing them.

Fields keep their protobuf types and defaults: an absent nested message reads as its default instance, not `null`, an absent string is empty, and an absent number is zero. The event models convert on top of that, so a user TikTok did not send has id `0` and an empty name. Enums are the generated enum classes, such as `ControlAction` on `WebcastControlMessage`; read them by name rather than by number in your own code, since the schema can be renumbered between releases.

## Schema definitions and verified behaviour

A message in the schema proves that TikTok's protocol can carry it and that this version can decode it. It does not prove that every room, region, or stream type sends it, that it arrives at the moment its name suggests, or what an undocumented integer field means. The schema defines messages for which no event exists, and event classes exist for messages the default mapper never registers; the grouped event pages say what each mapping produces, and a field that is always empty in your rooms may be filled elsewhere. Two mappings in the source carry a `not working` note from the maintainers, the poll events and `TikTokShopEvent`; see [room and display](/events/room-and-display/) and [games and commerce](/events/games-and-commerce/).

TikTok changes the protocol without notice. When a message stops arriving, a field moves, or a new name shows up in `TikTokWebsocketUnhandledMessageEvent`, compare with the latest release before assuming a bug in your code, and read the schema page of the message rather than the README for the current field list.

## Report a protocol issue

For a message the library does not know, or one it decodes wrongly, open an issue on the [TikTokLiveJava repository](https://github.com/jwdeveloper/TikTokLiveJava/issues) with the message's `method`, one or more Base64 payloads, the library version (`1.13.1-Release` for these pages), and what was happening in the stream. The payload is what lets the message be decoded and a schema written; a name alone is not enough. This handler prints both for every unmapped message:

```java
TikTokLive.newClient("isaackogz")
        .onWebsocketUnhandledMessage((client, event) ->
                System.out.println(event.getMessage().getMethod() + " "
                        + ProtocolUtils.toBase64(event.getMessage().getPayload().toByteArray())))
        .buildAndConnect();
```

Base64 is an encoding, not anonymization: a payload can contain usernames, ids, and other personal data, so review it before posting. See [troubleshooting](/troubleshooting/#report-a-reproducible-issue) for what else a report needs.
