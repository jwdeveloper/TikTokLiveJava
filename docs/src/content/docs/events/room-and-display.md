---
title: Room and display
description: Room information updates, pinned comments, in-room banners, the message header every message event carries, and polls.
sidebar:
  order: 6
---

Room information is kept in one `LiveRoomInfo` that the client updates in place and republishes; the other events here describe what is shown in the room. `TikTokHeaderEvent`, the base class of every message event, is documented here as well.

Each section names the Webcast message and links its schema page. Events with a builder method name it; the others are registered with `onEvent(TikTokXEvent.class, handler)`. Replace `isaackogz` with a creator who is live.

## TikTokRoomInfoEvent

Triggered when `LiveRoomInfo` got updated, such as likes, viewers, or ranking. Handler: `onRoomInfo`. Messages: [`WebcastRoomUserSeqMessage`](/schema/webcast/webcastroomuserseqmessage/) for the viewer counts and ranking, [`WebcastLiveIntroMessage`](/schema/webcast/webcastliveintromessage/) for the host and language, [`WebcastMemberMessage`](/schema/webcast/webcastmembermessage/) for the viewer count, and [`WebcastLikeMessage`](/schema/webcast/webcastlikemessage/) for the like count. The client also publishes it once at the end of `connect()` on the default path.

Key field: `getRoomInfo()`, the client's `LiveRoomInfo`, the same object `getRoomInfo()` returns, with `getRoomId()`, `getHostName()`, `getTitle()`, `getHost()`, `getStartTime()`, `isAgeRestricted()`, `getViewersCount()`, `getTotalViewersCount()`, `getLikesCount()`, `getLanguage()`, `getConnectionState()`, and `getUsersRanking()`, a list of `RankingUser` with `getRank()`, `getUser()`, and `getScore()`, sorted by score. The event extends `TikTokEvent` directly and has no message header. The [connection lifecycle](/getting-started/connection/#room-information) page describes which fields are filled before connecting.

## TikTokRoomPinEvent

A comment was pinned. Message: [`WebcastRoomPinMessage`](/schema/webcast/webcastroompinmessage/).

Key fields: `getPinnedMessage()`, a `TikTokCommentEvent` built from the pinned chat message, with its `getUser()` and `getText()`, and `getTimestamp()`, the pin time.

```java
TikTokLive.newClient("isaackogz")
        .onEvent(TikTokRoomPinEvent.class, (client, event) -> {
            var pinned = event.getPinnedMessage();
            System.out.println("Pinned: " + pinned.getUser().getProfileName() + ": " + pinned.getText());
        })
        .buildAndConnect();
```

## TikTokInRoomBannerEvent

An in-room banner, as the message's extra data. Message: [`WebcastInRoomBannerMessage`](/schema/webcast/webcastinroombannermessage/). Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).

Key field: `getJson()`, the message's extra map rendered with `toString()`, not JSON despite the name.

## TikTokHeaderEvent

The base class of the message events: message id, room id, and timestamp. It is mapped from the [`CommonMessageData`](/schema/data/commonmessagedata/) header every Webcast message carries and has no `@EventMeta` category; the client never publishes it on its own.

Key fields: `getMessageId()`, `getRoomId()`, and `getTimeStamp()`, the message's creation time. A few link events declare their own `getRoomId()` for the room the message is about; the pages note which.

## TikTokPollEvent

A poll message of a type with no dedicated event, and the base class of the three below. Message: [`WebcastPollMessage`](/schema/webcast/webcastpollmessage/).

Key field: `getPollId()`. The client picks the subclass from the message's [`MessageType`](/schema/enums/messagetype/): `MESSAGETYPE_SUBSUCCESS` becomes `TikTokPollStartEvent`, `MESSAGETYPE_ANCHORREMINDER` becomes `TikTokPollEndEvent`, `MESSAGETYPE_ENTERROOMEXPIRESOON` becomes `TikTokPollUpdateEvent`, and anything else this event. The library's source marks that mapping as unverified. A handler registered for `TikTokPollEvent.class` receives only the generic event.

## TikTokPollStartEvent

A poll started, with its title and options. Message: [`WebcastPollMessage`](/schema/webcast/webcastpollmessage/).

Key fields: `getPollId()`; `getTitle()`; `getOperator()`, the user who started it; and `getOptions()`, a list of `PollOption` with `getOptionId()`, `getContent()`, `getVotes()`, and `getUsers()`, the voters.

## TikTokPollUpdateEvent

A poll's vote counts were updated. Message: [`WebcastPollMessage`](/schema/webcast/webcastpollmessage/).

Key fields: `getPollId()` and `getOptions()`, the options with their current votes.

## TikTokPollEndEvent

A poll ended, with the final options. Message: [`WebcastPollMessage`](/schema/webcast/webcastpollmessage/).

Key fields: `getPollId()`, `getOperator()`, and `getOptions()`.
