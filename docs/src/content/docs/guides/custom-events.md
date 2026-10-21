---
title: Custom events and mappings
description: Publish your own TikTokEvent subclasses with publishEvent, map a Webcast message the default client leaves unmapped with mappings and forMessage, and wrap or replace a default mapping.
sidebar:
  order: 3
---

Two extension points add events. `publishEvent` dispatches any `TikTokEvent` subclass to the handlers and listeners, so your code can raise its own events from a handler or from anywhere else. `mappings` on the builder edits the table that turns Webcast messages into events, so a message the default client leaves unmapped can become an event, and a default mapping can be wrapped or replaced. This guide is based on the repository's `CustomEventExample.java` and `CustomMappingExample.java`. Replace `isaackogz` with a creator who is live.

## Publish your own events

A custom event is a class that extends `TikTokEvent`, an abstract class with no fields and a `toJson()` method. `client.publishEvent(event)` runs the `TikTokEvent` catch-all handlers first and then the handlers for the event's exact class, on the calling thread, before it returns, whether or not the client is connected. Subscribe with `onEvent(MyEvent.class, handler)` on the builder or with a listener method whose parameter is `MyEvent`; see [listeners](/guides/listeners/). `TikTokEmptyEvent` is a ready-made event with no fields for a plain trigger.

The example sorts gifts into two tiers and publishes a tier event from the gift handler. The repository's version declares the event classes with Lombok's `@AllArgsConstructor`; plain constructors do the same without annotation processing.

```java
import io.github.jwdeveloper.tiktok.api.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.api.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.client.TikTokLive;

public class GiftTiers {
    public static class CheapGiftEvent extends TikTokEvent {
        private final Gift gift;

        public CheapGiftEvent(Gift gift) {
            this.gift = gift;
        }

        public Gift getGift() {
            return gift;
        }
    }

    public static class ExpensiveGiftEvent extends TikTokEvent {
        private final Gift gift;
        private final int combo;

        public ExpensiveGiftEvent(Gift gift, int combo) {
            this.gift = gift;
            this.combo = combo;
        }

        public Gift getGift() {
            return gift;
        }

        public int getCombo() {
            return combo;
        }
    }

    public static void main(String[] args) {
        TikTokLive.newClient("isaackogz")
                .onGift((client, event) -> {
                    if (event.getGift().getDiamondCost() * event.getCombo() >= 100) {
                        client.publishEvent(new ExpensiveGiftEvent(event.getGift(), event.getCombo()));
                    } else {
                        client.publishEvent(new CheapGiftEvent(event.getGift()));
                    }
                })
                .onEvent(CheapGiftEvent.class, (client, event) ->
                        System.out.println("Thanks for the " + event.getGift().getName()))
                .onEvent(ExpensiveGiftEvent.class, (client, event) ->
                        System.out.println("Thank you for " + event.getCombo() + " x " + event.getGift().getName() + "!"))
                .buildAndConnect();
    }
}
```

An event published from inside a handler is dispatched before that handler returns. An exception thrown by a handler during `publishEvent` propagates to the caller of `publishEvent`; during message handling that is the message pipeline, which reports it as a `TikTokErrorEvent`, see [debugging](/guides/debugging/).

## Map a message the default client does not

The client maps a message only when a mapping is registered for its name; any other message is published as `TikTokWebsocketUnhandledMessageEvent`. Sixteen event classes exist for messages the default mapper does not register, listed under [unmapped events](/events/#unmapped-events). `mappings(mapper -> ...)` on the builder runs after the default mappings are registered and receives the `LiveMapper`:

- `forMessage(WebcastXMessage.class, action)` or `forMessage("WebcastXMessage", action)` registers `action` as the message's mapping; a class is keyed by its simple name.
- The action is a `MappingAction<MappingResult>`: it receives the payload bytes, the message name, and a `LiveMapperHelper`, and returns a `MappingResult` built with `MappingResult.of(message, event)` or `MappingResult.of(message, events)` for a list; `MappingResult.of(message)` publishes nothing.
- The helper deserialises: `bytesToWebcastObject(bytes, WebcastXMessage.class)`, or `bytesToWebcastObject(bytes, name)`, which looks the class up by name in `io.github.jwdeveloper.tiktok.api.messages.webcast`; `isMessageHasProtoClass(name)` says whether that class exists; `bytesToProtoBufferStructure(bytes)` decodes any payload into a `ProtoBufferObject` tree with `toJson()`; `toJson(object)` prints an object.

Each event in the result is published after a `TikTokWebsocketMessageEvent` that wraps it, like the events of a default mapping. The example maps `WebcastRankTextMessage` to `TikTokRankTextEvent`, whose constructor takes the message, as the catalog's example does; the other events on the unmapped list have the same kind of constructor, see the [API reference](/api/).

```java
import io.github.jwdeveloper.tiktok.api.messages.webcast.WebcastRankTextMessage;

TikTokLive.newClient("isaackogz")
        .mappings(mapper -> mapper.forMessage(WebcastRankTextMessage.class, (bytes, messageName, helper) -> {
            var message = helper.bytesToWebcastObject(bytes, WebcastRankTextMessage.class);
            return MappingResult.of(message, new TikTokRankTextEvent(message));
        }))
        .onEvent(TikTokRankTextEvent.class, (client, event) ->
                System.out.println(event.getEventType() + ": " + event.getLabel()))
        .buildAndConnect();
```

A mapping can also be a place to look at a message you do not have a class for yet. Registering a name takes the message out of the unhandled path, so `TikTokWebsocketUnhandledMessageEvent` no longer fires for it; return `MappingResult.of(bytes)` to publish nothing.

```java
TikTokLive.newClient("isaackogz")
        .mappings(mapper -> mapper.forMessage("WebcastSomeNewMessage", (bytes, messageName, helper) -> {
            if (helper.isMessageHasProtoClass(messageName)) {
                System.out.println(helper.bytesToWebcastObject(bytes, messageName));
            } else {
                System.out.println(helper.bytesToProtoBufferStructure(bytes).toJson());
            }
            return MappingResult.of(bytes);
        }))
        .buildAndConnect();
```

`forMessage(name)` without an action registers the name with a mapping that publishes nothing, so a call made only to set `onBeforeMapping` on a message the defaults do not map silences that message: it stops arriving as `TikTokWebsocketUnhandledMessageEvent` and produces no event. Give such a message an `onMapping` too.

## Wrap or replace a default mapping

`forMessage` returns the message's `TikTokMapperModel`, the existing one for a message the defaults already map, with three hooks. `onBeforeMapping(action)` receives the bytes and returns the bytes to map. `onMapping(action)` replaces the mapping, and the default is lost: after `forMessage(WebcastGiftMessage.class).onMapping(...)`, `onGift` no longer fires unless your mapping builds the gift events itself. `onAfterMapping(function)` receives the `MappingResult`, with `getSource()` (the deserialised message, as `Object`) and `getEvents()`, and returns the list of events to publish, so it can add, drop, or replace events while the default mapping keeps running. `forAnyMessage()` returns a model whose hooks run for every registered message after the message's own at each stage: its `onBeforeMapping` receives the bytes the message's hook returned, its `onAfterMapping` receives the events the message's hook returned, and its `onMapping` runs only when the message's mapping returns `null`. It never sees messages without a mapping.

```java
import io.github.jwdeveloper.tiktok.api.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.api.messages.webcast.WebcastChatMessage;
import io.github.jwdeveloper.tiktok.client.TikTokLive;

import java.util.ArrayList;

public class CommentLanguages {
    public static class ForeignCommentEvent extends TikTokEvent {
        private final String language;
        private final String text;

        public ForeignCommentEvent(String language, String text) {
            this.language = language;
            this.text = text;
        }

        public String getLanguage() {
            return language;
        }

        public String getText() {
            return text;
        }
    }

    public static void main(String[] args) {
        TikTokLive.newClient("isaackogz")
                .mappings(mapper -> mapper.forMessage(WebcastChatMessage.class)
                        .onAfterMapping(result -> {
                            var message = (WebcastChatMessage) result.getSource();
                            var events = new ArrayList<>(result.getEvents());
                            var language = message.getContentLanguage();
                            if (!language.isEmpty() && !language.equals("en")) {
                                events.add(new ForeignCommentEvent(language, message.getContent()));
                            }
                            return events;
                        }))
                .onComment((client, event) ->
                        System.out.println(event.getUser().getProfileName() + ": " + event.getText()))
                .onEvent(ForeignCommentEvent.class, (client, event) ->
                        System.out.println("[" + event.getLanguage() + "] " + event.getText()))
                .buildAndConnect();
    }
}
```

A mapping that throws is reported as a `TikTokErrorEvent` carrying a `TikTokLiveMessageException` when the message came from the socket, and thrown to the caller when it came from `publishMessage`; see [debugging](/guides/debugging/) and [offline testing](/guides/offline-testing/).
