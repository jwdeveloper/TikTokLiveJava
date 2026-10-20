---
title: Logging
description: The client's java.util.logging logger, the printToConsole and logLevel settings, what the client logs, and how to route the log to your own handler.
sidebar:
  order: 6
---

Each client logs through a `java.util.logging.Logger` named after its `hostName`, returned by `client.getLogger()`. The client is silent by default: `printToConsole` is `false` after `TikTokLive.newClient`, and that sets the logger to `Level.OFF`.

```java
TikTokLive.newClient("isaackogz")
        .configure(settings -> {
            settings.setPrintToConsole(true);
            settings.setLogLevel(Level.INFO);
        })
        .onConnected((client, event) ->
                client.getLogger().info("Connected to " + client.getRoomInfo().getHostName()))
        .buildAndConnect();
```

## Settings

| Field | Type | Default | Effect |
| --- | --- | --- | --- |
| `printToConsole` | `boolean` | `false` | When `false`, the logger's level is set to `Level.OFF` and nothing is logged. The field initialiser is `true`; `createDefault()` sets `false`. |
| `logLevel` | `java.util.logging.Level` | `Level.ALL` | The logger's level when `printToConsole` is `true`. |

Both are read once, when `build()` creates the logger. If a logger with that name already has a handler, from an earlier client for the same `hostName` in the same JVM, `build()` reuses it as it is and both settings of the new client are ignored.

## The console handler

When `build()` creates the logger, it adds a `ConsoleHandler`, which writes to `System.err`, with a formatter that prints `[hostName] [LEVEL]: message` in ANSI colours, and it stops the record from reaching the parent loggers, so the root logger's configuration does not apply. The handler keeps the JDK's default handler level, `INFO`: with `logLevel` at `Level.ALL`, records below `INFO` reach the logger but the handler drops them. Lower the handler's level to see them.

```java
var client = TikTokLive.newClient("isaackogz")
        .configure(settings -> settings.setPrintToConsole(true))
        .build();

for (var handler : client.getLogger().getHandlers()) {
    handler.setLevel(Level.ALL);
}
```

## What the client logs

At `INFO`: every state change as `TikTokLive client state: CONNECTING`, `CONNECTED`, or `DISCONNECTED`; `Reconnecting` before each retry; the `workerInfo` message on the Euler Stream WebSocket path. At `WARNING`: the signing response's headers when it lacks the `x-set-tt-cookie` header, and malformed JSON in the creator's user data, whether from TikTok or from the Euler Stream `roomInfo` message, with the exception attached. The HTTP client logs through a second logger named `HttpClient-<hash>`, created with the same settings.

Exceptions thrown by a handler, by the socket, or by the connect sequence are published as `TikTokErrorEvent` rather than logged; register `onError` to see them, see [connection lifecycle](/getting-started/connection/#lifecycle-events). An unexpected exception in `connect()` that is not a `TikTokLiveException` logs `Unhandled exception` at `INFO` and prints its stack trace to `System.err` regardless of the settings.

## Your own handler

The logger is a standard `java.util.logging.Logger`: remove the console handler and add your own, or forward it to a bridge for another logging framework. Set the logger's level too, because it is `Level.OFF` while `printToConsole` is `false`.

```java
var client = TikTokLive.newClient("isaackogz").build();
var logger = client.getLogger();

for (var handler : logger.getHandlers()) {
    logger.removeHandler(handler);
}
logger.addHandler(new FileHandler("tiktok-live.log", true));
logger.setLevel(Level.INFO);
```
