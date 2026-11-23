---
title: Moderation and access
description: Deleted messages, unauthorized members, detection probes, and the three fallback events for member, social, and control messages the client does not recognise.
sidebar:
  order: 8
---

Three events on this page describe moderation and access and are not produced by the default mapper. The other three are fallbacks: the member, social, and control messages each map to several events by their action or display text, and a value the client does not recognise becomes an unhandled wrapper holding the whole message. Each wrapper extends `TikTokUnhandledEvent` and exposes the message as `getData()`, so a new TikTok action can be inspected without a custom mapping.

Each section names the Webcast message and links its schema page. Register these events with `onEvent(TikTokXEvent.class, handler)`. Replace `isaackogz` with a creator who is live.

## TikTokIMDeleteEvent

Messages or users' messages were deleted. Message: [`WebcastImDeleteMessage`](/schema/webcast/webcastimdeletemessage/). Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).

Key fields: `getMsgIds()`, the ids of the deleted messages, and `getUserIds()`, the users whose messages were deleted; both are lists of `Long`.

## TikTokUnauthorizedMemberEvent

A viewer without access entered, with a nickname only. Message: [`WebcastUnauthorizedMemberMessage`](/schema/webcast/webcastunauthorizedmembermessage/). Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).

Key field: `getUserNickName()`.

## TikTokDetectEvent

A message-detection probe with its region. Message: [`WebcastMsgDetectMessage`](/schema/webcast/webcastmsgdetectmessage/). Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).

Key field: `getLanguage()`, which holds the message's from-region value.

## TikTokUnhandledMemberEvent

A member message with an action other than joined or subscribed. Message: [`WebcastMemberMessage`](/schema/webcast/webcastmembermessage/) whose [`MemberMessageAction`](/schema/enums/membermessageaction/) is neither `JOINED`, which becomes `TikTokJoinEvent`, nor `SUBSCRIBED`, which becomes `TikTokSubscribeEvent`.

Key field: `getData()`, the `WebcastMemberMessage`. The client still publishes a `TikTokRoomInfoEvent` with the message's member count.

## TikTokUnhandledSocialEvent

A social message that is not a like, follow, share, or join. Message: [`WebcastSocialMessage`](/schema/webcast/webcastsocialmessage/) whose display text key is none of `pm_mt_msg_viewer`, `pm_main_follow_message_viewer_2`, `pm_mt_guidance_share`, `pm_mt_join_message_other_viewer`, or the `pm_mt_guidance_viewer_<n>_share` pattern.

Key field: `getData()`, the `WebcastSocialMessage`; its common display text carries the key.

## TikTokUnhandledControlEvent

A control message with an action other than pause, unpause, end, or suspend. Message: [`WebcastControlMessage`](/schema/webcast/webcastcontrolmessage/) whose [`ControlAction`](/schema/enums/controlaction/) is not one of the four that [stream control](/events/stream-control/) maps.

Key field: `getData()`, the `WebcastControlMessage`.

```java
TikTokLive.newClient("isaackogz")
        .onEvent(TikTokUnhandledControlEvent.class, (client, event) -> System.out.println("Control action " + event.getData().getAction()))
        .buildAndConnect();
```
