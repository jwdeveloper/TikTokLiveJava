---
title: Subscriptions and fans
description: TikTokSubscribeEvent from both messages that carry a subscription, and the unmapped TikTokSubNotifyEvent.
sidebar:
  order: 5
---

A subscription reaches the client in two messages, and both become `TikTokSubscribeEvent`. Emotes, which only subscribers can send, are on [chat and audience](/events/chat-and-audience/#tiktokemoteevent); a subscription gift shown as a banner is a `TikTokBarrageEvent` on the same page.

Each section names the Webcast message and links its schema page. Replace `isaackogz` with a creator who is live.

## TikTokSubscribeEvent

Triggers when a user creates a subscription. Handler: `onSubscribe`. Messages: [`WebcastSubNotifyMessage`](/schema/webcast/webcastsubnotifymessage/), and [`WebcastMemberMessage`](/schema/webcast/webcastmembermessage/) with the action `SUBSCRIBED` of [`MemberMessageAction`](/schema/enums/membermessageaction/).

Key field: `getUser()`, with the `Subscriber` attribute added so that `isSubscriber()` is `true`. The member-message form also publishes a `TikTokRoomInfoEvent` with the member count. `TikTokSubscribeEvent.of(userName)` builds one for offline tests.

```java
TikTokLive.newClient("isaackogz")
        .onSubscribe((client, event) -> System.out.println(event.getUser().getProfileName() + " subscribed"))
        .buildAndConnect();
```

## TikTokSubNotifyEvent

A subscription notice with its user. Message: [`WebcastSubNotifyMessage`](/schema/webcast/webcastsubnotifymessage/). Not produced by the default mapper, which maps that message to `TikTokSubscribeEvent`; see [unmapped events](/events/#unmapped-events).

Key field: `getUser()`, without the `Subscriber` attribute that `TikTokSubscribeEvent` adds.
