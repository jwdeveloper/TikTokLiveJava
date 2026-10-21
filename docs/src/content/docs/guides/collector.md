---
title: Collect live data
description: Store every batch, message, event, and error of one or many livestreams as documents in JSON files or MongoDB with the extension-collector module's TikTokLiveCollector, filter what is stored, and reuse the stored payloads.
sidebar:
  order: 5
---

`TikTokLiveCollector`, in the `extension-collector` module, builds a `DataCollector` that writes what a client receives to a storage: each WebSocket batch, each message in it, each event, and each error, as one document apiece. One collector serves any number of clients through `newListener()`, so a program can archive several rooms into the same files or collection. The message payloads it stores are what `publishMessage` replays; see [offline testing](/guides/offline-testing/). This guide is based on the repository's `CollectorExample.java`. Replace `isaackogz` with a creator who is live.

## Install

The module is published on JitPack with the same group and version as the `Client` artifact; see [install](/getting-started/install/) for the repository declaration. It depends on `API`, `protobuf-java`, and `mongodb-driver-sync` 4.4.0, whose `Document` class is the document type, so the driver is pulled in even when writing to files. It does not depend on `Client`, so declare `Client` as well.

```xml
<dependency>
    <groupId>com.github.jwdeveloper.TikTok-Live-Java</groupId>
    <artifactId>extension-collector</artifactId>
    <version>1.13.1-Release</version>
</dependency>
```

```groovy
implementation 'com.github.jwdeveloper.TikTok-Live-Java:extension-collector:1.13.1-Release'
```

## Record to files

`TikTokLiveCollector.useFile(settings -> ...)` returns a `DataCollector` that writes JSON files. Call `connect()` before the clients start and `disconnect()` at the end; both are no-ops for files, but the Mongo storage needs them, so keep the calls. `newListener()` returns the listener to add to a client; `newListener(extraFields)` adds the map's entries to every document that listener writes, and `newListener(extraFields, filter)` also takes a `CollectorEvent`, `(client, document) -> boolean`, consulted before each document is stored.

| Setter | Type | Default | Effect |
| --- | --- | --- | --- |
| `setParentFile` | `File` | none | The directory the files go in. It must exist: a missing directory makes every write fail with a printed `IOException`. |
| `setTypeFilter` | `BiPredicate<String, String>` | accept all | Receives the document's `dataType` and `dataTypeName`; a `false` skips the document. |
| `setUserFilter` | `Predicate<String>` | accept all | Receives the document's `tiktokUser`. |
| `setUseFileLocks` | `boolean` | `false` | One lock per file around each write. Set it when several clients share the collector. |
| `setAppendUserName` | `boolean` | `false` | Adds `_<tiktokUser>` to the file names, one set of files per creator. |

Each document goes to `<dataType>_<dataTypeName>.json`, for example `message_WebcastChatMessage.json` or `event_TikTokCommentEvent.json`, appended as pretty-printed JSON followed by a newline. The file is a sequence of documents, not a JSON array, so read it with a parser that accepts concatenated values.

```java
import io.github.jwdeveloper.tiktok.extension.collector.TikTokLiveCollector;

var directory = new File("captures");
directory.mkdirs();

var collector = TikTokLiveCollector.useFile(settings -> {
    settings.setParentFile(directory);
    settings.setTypeFilter((dataType, dataTypeName) -> dataType.equals("message") || dataType.equals("error"));
});
collector.connect();

Map<String, Object> extraFields = Map.of("sessionTag", "monday-stream");

var client = TikTokLive.newClient("isaackogz")
        .onError((liveClient, event) -> event.getException().printStackTrace())
        .addListener(collector.newListener(extraFields))
        .buildAndConnect();

Thread.sleep(60_000);
client.disconnect();
collector.disconnect();
```

To archive several rooms at once, build one client per creator with the same collector, as the repository example does with `buildAndConnectAsync()`, and set `useFileLocks` or `appendUserName`.

## Record to MongoDB

`TikTokLiveCollector.useMongo(settings -> ...)` takes a `MongoDataCollectorSettings`: `setConnectionUrl(url)`, or `connectionBuilder(b -> b.setUsername(...).setPassword(...).setCluster(...).setDatabase(...))`, which builds a `mongodb+srv://` URL with `retryWrites=true&w=majority`; `setDatabaseName` (default `tiktok`); and `setCollectionName` (default `data`). `connect()` opens the client and creates hashed indexes on `session` and `dataType`; no document carries a `session` field unless you add one through the extra fields. Each document is stored with `insertOne`, and `disconnect()` closes the client.

```java
import io.github.jwdeveloper.tiktok.extension.collector.TikTokLiveCollector;

var collector = TikTokLiveCollector.useMongo(settings -> {
    settings.setConnectionUrl(System.getenv("MONGODB_URL"));
    settings.setDatabaseName("tiktok");
    settings.setCollectionName("live-data");
});
collector.connect();

var eventsOnly = collector.newListener(Map.of(), (liveClient, document) -> "event".equals(document.getString("dataType")));

TikTokLive.newClient("isaackogz")
        .addListener(eventsOnly)
        .buildAndConnect();
```

## What is stored

The listener has three observer methods, for `TikTokWebsocketResponseEvent`, every `TikTokEvent`, and `TikTokErrorEvent`. Every document has the same shape:

| Field | Value |
| --- | --- |
| `roomId` | The room id, `null` until the first `TikTokRoomInfoEvent`; the signing response's messages are dispatched before it, so the first documents of a connection have no room id. |
| extra fields | The entries of the map given to `newListener`. |
| `tiktokUser` | The creator's name, taken at `TikTokConnectingEvent`. |
| `dataType` | `response`, `message`, `event`, or `error`. |
| `dataTypeName` | `webcast` for a response; the Webcast message name for a message; the event's class name for an event; the exception's class name for an error. |
| `content` | Base64: the whole `ProtoMessageFetchResult` for a response; the message's payload bytes for a message; the event's JSON, from `toJson()`, for an event; the stack trace for an error. |
| `createdAt` | The local date-time, ISO format. |

A batch produces one `response` document and one `message` document per message in it. Every event produces an `event` document, including the control events, the debug events, and the ones you publish yourself, except `TikTokErrorEvent`, which produces an `error` document instead; for a `TikTokLiveMessageException` the error document also carries `message` and `response`, the failing message and its batch in Base64. The error observer prints the stack trace to `System.err` as well.

## Reuse the data

A `message` document is a replay unit: `dataTypeName` is the Webcast message name and `content` the Base64 payload that `client.publishMessage(dataTypeName, content)` takes, so a captured room can be run through handlers in offline mode. A `response` document holds a whole batch: decode it with `ProtoMessageFetchResult.parseFrom(Base64.getDecoder().decode(content))` and replay `getMessagesList()` in order. An `event` document decodes to the JSON that `toJson()` produced, which is for inspection; the event classes have no JSON constructor. The payloads of messages with no mapping are the material for a bug report, see [debugging](/guides/debugging/).
