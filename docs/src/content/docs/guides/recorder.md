---
title: Record a livestream
description: Save the live video with the extension-recorder module's TikTokLiveRecorder listener, set the output file and stop behaviour, react to the recorder's events, and what the recorder needs from the connection.
sidebar:
  order: 4
---

`TikTokLiveRecorder`, in the `extension-recorder` module, is a listener that downloads the stream's video to a file while the client is connected. It needs no FFmpeg: it reads the stream URL from the room page the client fetches while connecting and writes the bytes it receives, so the output is the FLV stream as TikTok sends it. This guide is based on the repository's `RecorderExample.java`. Replace `isaackogz` with a creator who is live.

## Install

The module is published on JitPack next to the `Client` artifact, with the same group and version, and depends on `Client`, so it brings the client with it; see [install](/getting-started/install/) for the repository declaration.

```xml
<dependency>
    <groupId>com.github.jwdeveloper.TikTok-Live-Java</groupId>
    <artifactId>extension-recorder</artifactId>
    <version>1.13.1-Release</version>
</dependency>
```

```groovy
implementation 'com.github.jwdeveloper.TikTok-Live-Java:extension-recorder:1.13.1-Release'
```

## Record from connect to disconnect

`TikTokLiveRecorder.use((recorderSettings, client) -> ...)` returns the listener; pass it to `addListener`. The consumer runs on `TikTokPreConnectionEvent`, when the room information is already filled, so the settings can depend on the room. What happens, in order:

1. On `TikTokPreConnectionEvent` the listener runs your consumer, then reads the room page JSON from the event and picks the stream: the first rendition present in the order `origin`, `hd`, `sd`, `ld`, `hd_60`, `uhd_60`, `ao`, and its `flv` URL, together with the session id. When no URL is found it logs a warning and, with `cancelConnectionIfNotFound` set, cancels the connection, so `connect()` throws `TikTokLivePreConnectionException`.
2. On `TikTokConnectedEvent` it publishes `TikTokLiveRecorderStartedEvent`; a handler can call `setCanceled(true)` on it to skip the recording. Otherwise it starts a background task that requests the URL with the client's default headers and appends every byte received to `outputFile`, creating its parent directories first, which is why the path must include a directory component: a bare filename has no parent to create, and the task fails before writing anything.
3. The task stops when the client's state leaves `CONNECTED` (with `stopOnDisconnect`, the default), when `TikTokDisconnectedEvent` or `TikTokLiveEndedEvent` arrives, or when TikTok closes the stream; then it logs `Stopped recording` and publishes `TikTokLiveRecorderEndedEvent`.

The two events live in `io.github.jwdeveloper.tiktok.extension.recorder.impl.event` and have no builder method; subscribe with `onEvent`. `TikTokLiveRecorderStartedEvent` has `getDownloadData()`, with `getDownloadLiveUrl()`, `getSessionId()`, and `getFullUrl()`, the URL requested; both events have `getSettings()`.

```java
import io.github.jwdeveloper.tiktok.extension.recorder.TikTokLiveRecorder;
import io.github.jwdeveloper.tiktok.extension.recorder.impl.event.TikTokLiveRecorderEndedEvent;
import io.github.jwdeveloper.tiktok.extension.recorder.impl.event.TikTokLiveRecorderStartedEvent;

TikTokLive.newClient("isaackogz")
        .configure(settings -> settings.setPrintToConsole(true))
        .onError((client, event) -> event.getException().printStackTrace())
        .addListener(TikTokLiveRecorder.use((recorderSettings, client) -> {
            recorderSettings.setOutputFile(new File("recordings", client.getRoomInfo().getHostName() + ".flv"));
            recorderSettings.setCancelConnectionIfNotFound(true);
        }))
        .onEvent(TikTokLiveRecorderStartedEvent.class, (client, event) ->
                System.out.println("Recording " + event.getDownloadData().getFullUrl()))
        .onEvent(TikTokLiveRecorderEndedEvent.class, (client, event) ->
                System.out.println("Saved " + event.getSettings().getOutputFile()))
        .buildAndConnect();
```

The socket thread keeps the process alive while the client is connected, and the download runs on the common pool, so a `main` that ends with `buildAndConnect()` records until the stream ends or the client disconnects.

## Settings

`RecorderSettings` is the object the consumer receives.

| Setter | Type | Default | Effect |
| --- | --- | --- | --- |
| `setOutputFile` | `File` | none | The file the stream is appended to; its parent directories are created. Required, and the path must include a directory component, such as `new File("recordings", "clip.flv")`: without a file, or with a bare filename such as `new File("clip.flv")`, the download task fails with a printed `NullPointerException` and no ended event. A second recording by the same listener appends to the same file. |
| `setStopOnDisconnect` | `boolean` | `true` | Stop writing as soon as the client's state is no longer `CONNECTED`, and on `TikTokDisconnectedEvent`. With `false`, the recording runs until the stream ends or TikTok closes it. |
| `setCancelConnectionIfNotFound` | `boolean` | `false` | Cancel the connection from `TikTokPreConnectionEvent` when no stream URL is found, instead of connecting without recording. |
| `setPrepareDownloadData` | `Function<String, DownloadData>` | none | Replaces the URL lookup. Receives the room page JSON and returns `new DownloadData(url, sessionId)`; `getFullUrl()` appends `_webnoredir=1&_session_id=<sessionId>` to the URL. |

`TikTokLiveRecorder.use()` without a consumer exists, but it leaves `outputFile` unset, so use the consumer form. `LiveQuality` and `LiveFormat` enums exist in `impl.enums`, but the quality and format settings are commented out in this release: the first rendition found in the order above is used, and the output is always the FLV stream.

## Stop a recording

`LiveRecorder`, the type `use` returns, has no stop method. Disconnecting the client stops the recording when `stopOnDisconnect` is `true`: the download loop checks the connection state on every chunk, so it ends at the next one. While a recording runs, a further `TikTokConnectedEvent` is ignored, so one listener records one stream at a time.

```java
import io.github.jwdeveloper.tiktok.extension.recorder.TikTokLiveRecorder;

var client = TikTokLive.newClient("isaackogz")
        .addListener(TikTokLiveRecorder.use((recorderSettings, liveClient) ->
                recorderSettings.setOutputFile(new File("recordings", "clip.flv"))))
        .buildAndConnect();

Thread.sleep(60_000);
client.disconnect();
```

The file is the FLV stream as received, which VLC and `ffplay` open directly. To change the container without re-encoding:

```sh
ffmpeg -i recordings/clip.flv -c copy clip.mp4
```

## Requirements and limits

- The recorder needs the default connection path. With `useEulerstreamWebsocket`, no `TikTokPreConnectionEvent` is published, so the URL is never looked up, and the listener's `TikTokConnectedEvent` method then fails with a `NullPointerException`, reported as a `TikTokErrorEvent`; see [signing](/configuration/signing/).
- The stream URL comes from `data.liveRoom.streamData.pull_data.stream_data` in the room page. When TikTok changes that layout the lookup finds nothing and logs `Unable to find download live url!`; `setPrepareDownloadData` is the way to supply the URL yourself.
- Everything the listener logs goes through the client's logger, so it is silent unless `printToConsole` is on; see [logging](/configuration/logging/).
- The repository example sets `setFfmpegPath` in a comment; no such setter exists in this release.
