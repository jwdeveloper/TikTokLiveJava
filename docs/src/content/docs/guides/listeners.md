---
title: Listeners
description: Register handlers as annotated methods of a listener class with addListener, choose the method parameters, set priority and async on @TikTokEventObserver, add and remove listeners at runtime, and what happens when a method throws.
sidebar:
  order: 2
---

A listener is any object whose methods carry `@TikTokEventObserver`. `addListener(listener)` on the builder registers it when `build()` runs; `client.getListenersManager().addListener(listener)` registers one on a built client. Each annotated method becomes a handler for the event class of its parameter, the same as a lambda passed to `onEvent`, so a listener is the way to keep related handlers, and the state they share, together in one class instead of a chain of lambdas. This guide is based on the repository's `ListenerExample.java`. Replace `isaackogz` with a creator who is live.

## Write a listener

The listeners manager scans the methods declared on the listener's class and registers every one that carries `@TikTokEventObserver` and has at least one parameter. The event class is the first parameter whose type extends `TikTokEvent`; the other parameters are resolved from the client's container, so a `LiveClient` parameter can come first, second, or be left out. The method may be private: the manager makes it accessible. A method that carries the annotation but has no `TikTokEvent` parameter fails registration with `TikTokEventListenerMethodException`, which `addListener` throws, so from `build()` for a listener added on the builder.

```java
import io.github.jwdeveloper.tiktok.api.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.api.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.api.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.api.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.api.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.api.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.api.live.LiveClient;
import io.github.jwdeveloper.tiktok.client.TikTokLive;

public class ChatListener {
    private int likes;

    @TikTokEventObserver
    public void onComment(LiveClient client, TikTokCommentEvent event) {
        client.getLogger().info(event.getUser().getName() + ": " + event.getText());
    }

    @TikTokEventObserver
    public void onGift(LiveClient client, TikTokGiftEvent event) {
        var message = switch (event.getGift().getName()) {
            case "Rose" -> "Thanks for the rose!";
            case "TikTok" -> "A TikTok gift, nice!";
            default -> "Thanks for the " + event.getGift().getName();
        };
        client.getLogger().info(message);
    }

    @TikTokEventObserver
    public void onLike(TikTokLikeEvent event) {
        likes += event.getLikes();
    }

    @TikTokEventObserver
    private void onError(TikTokErrorEvent event, LiveClient client) {
        client.getLogger().warning("Error: " + event.getException().getMessage());
    }

    @TikTokEventObserver
    public void onAnyEvent(LiveClient client, TikTokEvent event) {
        client.getLogger().info(event.getClass().getSimpleName() + ", " + likes + " likes so far");
    }

    public static void main(String[] args) {
        TikTokLive.newClient("isaackogz")
                .configure(settings -> settings.setPrintToConsole(true))
                .addListener(new ChatListener())
                .buildAndConnect();
    }
}
```

A method is called for events of exactly its parameter's class, plus the `TikTokEvent` catch-all: `onGift` above is not called for a `TikTokGiftComboEvent`, and a method taking `TikTokLinkEvent` receives only the generic link event, not `TikTokLinkInviteEvent`. A method whose parameter is `TikTokEvent` receives every event, including the debug events and the ones you publish yourself; see the [event catalog](/events/). Listener methods run on the thread that publishes the event, which for socket messages is the socket's thread, so a slow method delays the messages behind it.

## Priority and asynchronous methods

`@TikTokEventObserver` has two attributes. `priority`, a `Priority` from `HIGHEST` to `LOWEST` (default `NORMAL`), sorts the listener's methods when the listener is registered. In this release that is as far as it goes: the event handler stores the handlers of each event class in a `HashSet` and runs them by iterating it, so the order in which several handlers for one event run, lambdas or listener methods, from one listener or several, is unspecified, and `priority` does not fix it. Write handlers that do not depend on each other's order.

`async = true` runs the method on an `ExecutorService` instead of the publishing thread. The executor is the one set with `getListenersManager().setAsyncExecutor(executor)` or, when none is set, a cached thread pool created the first time an asynchronous method is registered. The executor is captured when the listener is registered, and listeners added on the builder are registered during `build()`, before the manager can be reached; so to use your own executor, build the client, set the executor, then add the listener through the manager.

```java
import io.github.jwdeveloper.tiktok.api.annotations.Priority;
import io.github.jwdeveloper.tiktok.api.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.api.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.api.live.LiveClient;
import io.github.jwdeveloper.tiktok.client.TikTokLive;

import java.util.concurrent.Executors;

public class AsyncListener {
    @TikTokEventObserver(async = true, priority = Priority.LOW)
    public void onComment(LiveClient client, TikTokCommentEvent event) {
        // A slow lookup here no longer delays the next message
        client.getLogger().info(Thread.currentThread().getName() + " handled: " + event.getText());
    }

    public static void main(String[] args) {
        var client = TikTokLive.newClient("isaackogz")
                .configure(settings -> settings.setPrintToConsole(true))
                .build();
        client.getListenersManager().setAsyncExecutor(Executors.newFixedThreadPool(4));
        client.getListenersManager().addListener(new AsyncListener());
        client.connect();
    }
}
```

## Add and remove listeners at runtime

`client.getListenersManager()` returns the `ListenersManager`: `addListener(Object)`, `removeListener(Object)`, which unsubscribes every method of that object, and `getListeners()`. Registering the same object twice throws `TikTokLiveException`; a second instance of the same class is a separate listener. Removing an object that was never added does nothing. Any object with annotated methods qualifies, an anonymous class included.

```java
var client = TikTokLive.newClient("isaackogz").build();
var manager = client.getListenersManager();

var greeter = new Object() {
    @TikTokEventObserver
    public void onJoin(LiveClient liveClient, TikTokJoinEvent event) {
        liveClient.getLogger().info("Welcome " + event.getUser().getProfileName());
    }
};

manager.addListener(greeter);
client.connect();
System.out.println(manager.getListeners().size() + " listener(s) registered");
manager.removeListener(greeter);
```

## When a method throws

An exception thrown by a listener method does not reach the publisher: the manager catches it and publishes a `TikTokErrorEvent` whose exception is a `TikTokEventListenerMethodException` wrapping the reflective call's `InvocationTargetException`, so your exception is at the end of the cause chain. The client then carries on with the next handler and the next message. Do not throw from a method that handles `TikTokErrorEvent`: its failure publishes another `TikTokErrorEvent`, which calls it again.

```java
TikTokLive.newClient("isaackogz")
        .onError((client, event) -> {
            var exception = event.getException();
            if (exception instanceof TikTokEventListenerMethodException) {
                var cause = exception;
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                }
                System.err.println("A listener method failed: " + cause);
            }
        })
        .buildAndConnect();
```

## TikTokEventListenerBase

`TikTokEventListenerBase` is an abstract class with an empty method per event, `onComment(LiveClient, TikTokCommentEvent)`, `onGift(LiveClient, TikTokGiftEvent)`, and so on, as a list of the signatures to override. Extending it registers nothing by itself: the manager scans only the annotated methods declared on your class, and the base's methods carry no annotation. Annotate each override.

```java
import io.github.jwdeveloper.tiktok.api.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.api.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.api.listener.TikTokEventListenerBase;
import io.github.jwdeveloper.tiktok.api.live.LiveClient;

public class FollowListener extends TikTokEventListenerBase {
    @Override
    @TikTokEventObserver
    public void onFollow(LiveClient client, TikTokFollowEvent event) {
        client.getLogger().info(event.getUser().getProfileName() + " followed");
    }
}
```
