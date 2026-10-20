---
title: Event catalog
description: Every event TikTokLiveJava emits, its category, what it means, and the page that documents it, with both ways to subscribe.
sidebar:
  order: 0
---

A handler is registered in one of two forms. The builder's `on<Event>` methods take an `EventConsumer`, a lambda that receives the `LiveClient` and the event; `onEvent` does the same for any event class, including the ones without a dedicated method, as `onEvent(TikTokXEvent.class, handler)`, and `onEvent(handler)` receives every event. The other form is a listener: any object whose methods carry `@TikTokEventObserver` and take a parameter that extends `TikTokEvent`, added with `addListener` on the builder or on `getListenersManager()`; the `LiveClient` is an optional second parameter in either position. See [listeners](/guides/listeners/) for priorities and asynchronous methods.

```java
import io.github.jwdeveloper.tiktok.api.annotations.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.api.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.api.data.events.social.TikTokShareEvent;
import io.github.jwdeveloper.tiktok.api.live.LiveClient;
import io.github.jwdeveloper.tiktok.client.TikTokLive;

public class EventForms {
    public static void main(String[] args) {
        TikTokLive.newClient("isaackogz")
                .onComment((client, event) -> System.out.println(event.getUser().getProfileName() + ": " + event.getText()))
                .onEvent(TikTokShareEvent.class, (client, event) -> System.out.println(event.getUser().getProfileName() + " shared the stream"))
                .addListener(new ReactionListener())
                .buildAndConnect();
    }

    public static class ReactionListener {
        @TikTokEventObserver
        public void onLike(LiveClient client, TikTokLikeEvent event) {
            client.getLogger().info(event.getUser().getProfileName() + " sent " + event.getLikes() + " likes");
        }
    }
}
```

Handlers are matched by the event's exact class, plus the `TikTokEvent` catch-all. A handler for a base class does not receive its subclasses: `onGift` is not called for a `TikTokGiftComboEvent` although that class extends `TikTokGiftEvent`, and `onEvent(TikTokLinkEvent.class, ...)` receives only the generic link event, not `TikTokLinkInviteEvent`. An exception thrown by a handler while a message is processed is published as `TikTokErrorEvent`, and the client carries on with the next message. Register handlers before connecting: the signing response already carries messages, and they are dispatched before the socket opens.

This checkout defines 78 event classes under `io.github.jwdeveloper.tiktok.api.data.events`. The `@EventMeta` annotation on each gives its category: 6 `Control` events, which the client publishes about its own connection; 64 `Message` events, mapped from TikTok's Webcast protobuf messages; and 4 `Debug` events, which expose the raw traffic. Four classes carry no category: `TikTokLiveClientEvent` and `TikTokHeaderEvent` are the base classes of the control and message events, `TikTokUnhandledEvent` is the base of the unhandled wrappers, and `TikTokEmptyEvent` is a placeholder. `TikTokEvent` itself is abstract and is never published; it is the type `onEvent(handler)` receives, and every event inherits its `toJson()`.

A message event extends `TikTokHeaderEvent`, so it carries `getMessageId()`, `getRoomId()`, and `getTimeStamp()` from the message's common header, and each grouped page links the message's [schema page](/schema/) for the full field list. Nested objects such as `getUser()` and `getGift()` are mapped from the message and are not `null`; a user TikTok did not send has id `0` and an empty name. Some messages produce more than one event: a like message publishes `TikTokLikeEvent` and then `TikTokRoomInfoEvent`, and a finished gift streak publishes `TikTokGiftComboEvent` and then `TikTokGiftEvent`. Every mapped event is preceded by a `TikTokWebsocketMessageEvent` that wraps it, and every batch by a `TikTokWebsocketResponseEvent`; see [debugging](/events/debugging/).

## Unmapped events

The client maps a message only when `MessagesMapperFactory` registers a handler for its name; any other message is published as `TikTokWebsocketUnhandledMessageEvent` with its raw payload. Sixteen event classes exist for messages the default mapper does not register, or for a case its switch does not cover, so the default client never produces them: `TikTokChestEvent`, `TikTokDetectEvent`, `TikTokEnvelopeEvent`, `TikTokGoalUpdateEvent`, `TikTokIMDeleteEvent`, `TikTokInRoomBannerEvent`, `TikTokLinkApplyEvent`, `TikTokLinkLayerEvent`, `TikTokLinkMicFanTicketEvent`, `TikTokLinkMicMethodEvent`, `TikTokLinkReplyEvent`, `TikTokRankTextEvent`, `TikTokRankUpdateEvent`, `TikTokShopEvent`, `TikTokSubNotifyEvent`, and `TikTokUnauthorizedMemberEvent`. Each section below says so where it applies. Two more are never published by the client for another reason: `TikTokHttpResponseEvent` has a handler but no publisher in this checkout, and `TikTokEmptyEvent` is referenced nowhere.

To produce one of them, register a mapping with `mappings` on the builder. The mapping receives the message bytes, deserialises them with the helper, and returns a `MappingResult` holding the message and the events to publish. The protobuf classes live in `io.github.jwdeveloper.tiktok.api.messages.webcast`.

```java
import io.github.jwdeveloper.tiktok.api.data.events.TikTokRankTextEvent;
import io.github.jwdeveloper.tiktok.api.mappers.data.MappingResult;
import io.github.jwdeveloper.tiktok.api.messages.webcast.WebcastRankTextMessage;

TikTokLive.newClient("isaackogz")
        .mappings(mapper -> mapper.forMessage(WebcastRankTextMessage.class, (bytes, messageName, helper) -> {
            var message = helper.bytesToWebcastObject(bytes, WebcastRankTextMessage.class);
            return MappingResult.of(message, new TikTokRankTextEvent(message));
        }))
        .onEvent(TikTokRankTextEvent.class, (client, event) -> System.out.println(event.getLabel()))
        .buildAndConnect();
```

A mapping registered this way replaces the default one for that message, so a custom `WebcastLinkMessage` mapping must reproduce the cases it still wants.

## Find an event

### Control

The client's own connection lifecycle. Details: [Control events](/events/control/).

| Event | Type | Description |
| --- | --- | --- |
| `TikTokConnectingEvent` | Control | The first thing `connect()` does, before any request |
| `TikTokPreConnectionEvent` | Control | The room data is fetched and the connection can still be cancelled |
| `TikTokConnectedEvent` | Control | Triggered when the connection is successfully established |
| `TikTokReconnectingEvent` | Control | A scheduled retry is about to call `connect()` again |
| `TikTokDisconnectedEvent` | Control | Triggered when the connection gets disconnected |
| `TikTokErrorEvent` | Control | General error event; you should handle this |
| `TikTokLiveClientEvent` | none | Base class of the control events, never published on its own |

### Chat and audience

Comments, emotes, joins, likes, follows, shares, questions, barrages, and captions. Details: [Chat and audience](/events/chat-and-audience/).

| Event | Type | Description |
| --- | --- | --- |
| `TikTokCommentEvent` | Message | Triggered every time a new chat comment arrives |
| `TikTokEmoteEvent` | Message | Triggered every time a subscriber sends an emote (sticker) |
| `TikTokJoinEvent` | Message | A viewer joined the livestream |
| `TikTokLikeEvent` | Message | Triggered when a viewer sends likes to the streamer |
| `TikTokFollowEvent` | Message | Triggers when a user follows the streamer |
| `TikTokShareEvent` | Message | Triggers when a user shares the stream |
| `TikTokQuestionEvent` | Message | Triggered every time someone asks a new question via the question feature |
| `TikTokBarrageEvent` | Message | An animated banner notice, such as a high-grade viewer entering, a fan-level upgrade, or a subscription gift |
| `TikTokCaptionEvent` | Message | Captions of the host's speech |

### Gifts and goals

Gifts, streaks, goals, envelopes, and treasure chests. Details: [Gifts and goals](/events/gifts-and-goals/).

| Event | Type | Description |
| --- | --- | --- |
| `TikTokGiftEvent` | Message | Triggered when a user sends a gift that has no combo, or when a combo has finished |
| `TikTokGiftComboEvent` | Message | Triggered every time a gift is sent, with the streak's state |
| `TikTokGoalUpdateEvent` | Message | A goal's progress was updated ([unmapped](/events/#unmapped-events)) |
| `TikTokEnvelopeEvent` | Message | An envelope, the sender of a treasure chest ([unmapped](/events/#unmapped-events)) |
| `TikTokChestEvent` | Message | A treasure chest with its diamond total and participant count ([unmapped](/events/#unmapped-events)) |

### Battles and cohosting

Battles, battle points, and link-mic (cohost and multi-guest) signalling. Details: [Battles and cohosting](/events/battles-and-cohosting/).

| Event | Type | Description |
| --- | --- | --- |
| `TikTokLinkMicBattleEvent` | Message | A battle started or finished, with its teams, hosts, and final scores |
| `TikTokLinkMicArmiesEvent` | Message | A battle's points were updated, per host or team with the top contributors |
| `TikTokLinkMicBattleItemCard` | Message | A battle Power-Up card notice; carries only the message header |
| `TikTokLinkMicFanTicketEvent` | Message | A fan-ticket notice of a link-mic session; carries only the header ([unmapped](/events/#unmapped-events)) |
| `TikTokLinkMicMethodEvent` | Message | Link-mic signalling between hosts; carries only the header ([unmapped](/events/#unmapped-events)) |
| `TikTokLinkLayerEvent` | Message | A link layer change; carries only the header ([unmapped](/events/#unmapped-events)) |
| `TikTokLinkEvent` | Message | A link message of a type with no dedicated event, and the base class of the ones below |
| `TikTokLinkAcceptNoticeEvent` | Message | An invitation was accepted |
| `TikTokLinkApplyEvent` | Message | A guest applied to join ([unmapped](/events/#unmapped-events)) |
| `TikTokLinkCancelEvent` | Message | An invitation or application was cancelled |
| `TikTokLinkCloseEvent` | Message | The link session was closed |
| `TikTokLinkCohostListChangeEvent` | Message | The cohost list changed |
| `TikTokLinkCreateEvent` | Message | A link session was created |
| `TikTokLinkEnterEvent` | Message | Users entered the link session, with the linked users list |
| `TikTokLinkInviteEvent` | Message | A host invited another host |
| `TikTokLinkKickOutEvent` | Message | A guest was removed, with the reason |
| `TikTokLinkLeaveEvent` | Message | A guest left |
| `TikTokLinkLinkedListChangeEvent` | Message | The linked users list changed (first version of the message) |
| `TikTokLinkListChangeEvent` | Message | The linked, applied, and connecting user lists changed |
| `TikTokLinkMediaChangeEvent` | Message | A guest's microphone or camera was opened or closed |
| `TikTokLinkMicIdxUpdateEvent` | Message | A guest's microphone position changed |
| `TikTokLinkMuteEvent` | Message | A guest was muted or unmuted |
| `TikTokLinkRandomMatchEvent` | Message | A random match paired the host with another host |
| `TikTokLinkReplyEvent` | Message | A host replied to an invitation ([unmapped](/events/#unmapped-events)) |
| `TikTokLinkSysKickOutEvent` | Message | The system removed a guest |
| `TikTokLinkUpdateUserEvent` | Message | A linked user's state was updated |
| `TikTokLinkUpdateUserSettingEvent` | Message | A linked user's settings were updated |
| `TikTokLinkUserToastEvent` | Message | A toast message about a linked user |
| `TikTokLinkWaitListChangeEvent` | Message | The waiting list changed |

### Subscriptions and fans

Subscriptions and subscription notices. Details: [Subscriptions and fans](/events/subscriptions-and-fans/).

| Event | Type | Description |
| --- | --- | --- |
| `TikTokSubscribeEvent` | Message | Triggers when a user creates a subscription |
| `TikTokSubNotifyEvent` | Message | A subscription notice with its user ([unmapped](/events/#unmapped-events)) |

### Room and display

Room information, pinned messages, banners, polls, and the message header. Details: [Room and display](/events/room-and-display/).

| Event | Type | Description |
| --- | --- | --- |
| `TikTokRoomInfoEvent` | Message | Triggered when `LiveRoomInfo` got updated, such as likes, viewers, or ranking |
| `TikTokRoomPinEvent` | Message | A comment was pinned |
| `TikTokInRoomBannerEvent` | Message | An in-room banner, as the message's extra data ([unmapped](/events/#unmapped-events)) |
| `TikTokHeaderEvent` | none | Base class of the message events: message id, room id, and timestamp |
| `TikTokPollEvent` | Message | A poll message of a type with no dedicated event, and the base class of the three below |
| `TikTokPollStartEvent` | Message | A poll started, with its title and options |
| `TikTokPollUpdateEvent` | Message | A poll's vote counts were updated |
| `TikTokPollEndEvent` | Message | A poll ended, with the final options |

### Rankings

Rank text and rank updates. Details: [Rankings](/events/rankings/).

| Event | Type | Description |
| --- | --- | --- |
| `TikTokRankTextEvent` | Message | A rank notice's text, such as a viewer entering the top contributors ([unmapped](/events/#unmapped-events)) |
| `TikTokRankUpdateEvent` | Message | The room's rank or hourly rank was updated; carries only the header ([unmapped](/events/#unmapped-events)) |

### Moderation and access

Deleted messages, unauthorized members, detection probes, and the fallbacks for member, social, and control messages. Details: [Moderation and access](/events/moderation-and-access/).

| Event | Type | Description |
| --- | --- | --- |
| `TikTokIMDeleteEvent` | Message | Messages or users' messages were deleted ([unmapped](/events/#unmapped-events)) |
| `TikTokUnauthorizedMemberEvent` | Message | A viewer without access entered, with a nickname only ([unmapped](/events/#unmapped-events)) |
| `TikTokDetectEvent` | Message | A message-detection probe with its region ([unmapped](/events/#unmapped-events)) |
| `TikTokUnhandledMemberEvent` | Message | A member message with an action other than joined or subscribed |
| `TikTokUnhandledSocialEvent` | Message | A social message that is not a like, follow, share, or join |
| `TikTokUnhandledControlEvent` | Message | A control message with an action other than pause, unpause, end, or suspend |

### Games and commerce

Live shopping. Details: [Games and commerce](/events/games-and-commerce/).

| Event | Type | Description |
| --- | --- | --- |
| `TikTokShopEvent` | Message | A product shown in live shopping ([unmapped](/events/#unmapped-events)) |

### Stream control

Stream pause, resume, end, and suspension. Details: [Stream control](/events/stream-control/).

| Event | Type | Description |
| --- | --- | --- |
| `TikTokLiveEndedEvent` | Message | Triggered when the live stream gets terminated by the host, or suspended by TikTok |
| `TikTokLivePausedEvent` | Message | The host paused the stream |
| `TikTokLiveUnpausedEvent` | Message | The host resumed a paused stream |

### Debugging

The raw traffic, the mapping of each message, and the generic base classes. Details: [Debugging](/events/debugging/).

| Event | Type | Description |
| --- | --- | --- |
| `TikTokWebsocketResponseEvent` | Debug | A batch of messages arrived, before any of them is mapped |
| `TikTokWebsocketMessageEvent` | Debug | Triggered every time TikTok sends data, with the message, its event, and the mapping time |
| `TikTokWebsocketUnhandledMessageEvent` | Debug | A protobuf-encoded Webcast message arrived that no mapping is registered for |
| `TikTokHttpResponseEvent` | Debug | An HTTP request and its response; not published in this checkout |
| `TikTokUnhandledEvent` | none | Generic base class of the unhandled wrappers, holding the raw message |
| `TikTokEmptyEvent` | none | An event with no fields, for your own `publishEvent` calls |
