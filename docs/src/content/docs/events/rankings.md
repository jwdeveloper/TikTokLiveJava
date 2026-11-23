---
title: Rankings
description: Rank text and rank update events, which exist in the API but need a custom mapping to be produced.
sidebar:
  order: 7
---

Ranking messages describe the room's position on TikTok's leaderboards and a viewer's position among the room's contributors. The default mapper registers neither message, so both events below reach your handlers only through a custom mapping; the [catalog](/events/#unmapped-events) shows one for `TikTokRankTextEvent`. The room's own contributor ranking arrives without a mapping, as `getUsersRanking()` on `TikTokRoomInfoEvent`; see [room and display](/events/room-and-display/#tiktokroominfoevent).

Each section names the Webcast message and links its schema page. Register these events with `onEvent(TikTokXEvent.class, handler)`.

## TikTokRankTextEvent

A rank notice's text, such as a viewer entering the top contributors. Message: [`WebcastRankTextMessage`](/schema/webcast/webcastranktextmessage/). Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).

Key fields: `getEventType()`, the key of the message's self-get-badge text, and `getLabel()`, that text's default pattern, with placeholders unfilled.

## TikTokRankUpdateEvent

The room's rank or hourly rank was updated; the event carries only the message header. Messages: [`WebcastRankUpdateMessage`](/schema/webcast/webcastrankupdatemessage/) and [`WebcastHourlyRankMessage`](/schema/webcast/webcasthourlyrankmessage/), with one constructor for each. Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).
