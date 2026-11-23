---
title: Chat and audience
description: Comments, emotes, joins, likes, follows, shares, questions, barrages, and captions, with the fields each event carries and examples for comments and likes.
sidebar:
  order: 2
---

Chat, membership, reactions, and social activity. The sender is `getUser()`, a `User` with `getId()`, `getName()` (the username), `getProfileName()` (the display name), `getPicture()`, `getFollowers()`, `getFollowing()`, `getBadges()`, and the attribute checks `isModerator()`, `isSubscriber()`, `isGiftGiver()`, `isFollower()`, `isLiveHost()`, `isMutualFollowingWithHost()`, `isAdmin()`, `isMuted()`, and `isBlocked()`. Which attributes are set depends on the message: every message sets `isFollower()`, `isSubscriber()`, `isAdmin()`, `isMuted()`, and `isBlocked()` from the user object; comments and gifts also carry a user identity, which adds `isModerator()`, `isGiftGiver()`, `isLiveHost()`, and `isMutualFollowingWithHost()`, so those four are `false` on every other message.

Each section names the Webcast message and links its schema page, which lists every field. Events with a builder method name it; the others are registered with `onEvent(TikTokXEvent.class, handler)`. Replace `isaackogz` with a creator who is live.

## TikTokCommentEvent

Triggered every time a new chat comment arrives. Handler: `onComment`. Message: [`WebcastChatMessage`](/schema/webcast/webcastchatmessage/).

Key fields: `getText()`, the comment; `getUser()`, the sender with the identity attributes; `getUserLanguage()`, the comment's content language; `getMentionedUser()`, the user a reply is addressed to, with id `0` when there is none; `getPictures()`, the images of the emotes in the comment as `Picture` objects whose `getLink()` is the URL; and `isVisibleToSender()`, set when TikTok showed the comment only to its sender. A pinned comment arrives again inside `TikTokRoomPinEvent` on [room and display](/events/room-and-display/#tiktokroompinevent). `TikTokCommentEvent.of(userName, text)` builds one for offline tests.

```java
TikTokLive.newClient("isaackogz")
        .onComment((client, event) -> {
            var user = event.getUser();
            var badge = user.isModerator() ? " [mod]" : user.isSubscriber() ? " [sub]" : "";
            System.out.println(user.getProfileName() + badge + ": " + event.getText());
        })
        .buildAndConnect();
```

## TikTokEmoteEvent

Triggered every time a subscriber sends an emote (sticker). Handler: `onEmote`. Message: [`WebcastEmoteChatMessage`](/schema/webcast/webcastemotechatmessage/).

Key fields: `getUser()` and `getEmotes()`, a list of `Emote` with `getEmoteId()` and `getPicture()`. Emotes sent inline in a comment arrive as `getPictures()` on `TikTokCommentEvent` instead.

## TikTokJoinEvent

A viewer joined the livestream. Handler: `onJoin`. Messages: [`WebcastMemberMessage`](/schema/webcast/webcastmembermessage/) with the action `JOINED` of [`MemberMessageAction`](/schema/enums/membermessageaction/), and [`WebcastSocialMessage`](/schema/webcast/webcastsocialmessage/) whose display text key is `pm_mt_join_message_other_viewer`.

Key fields: `getUser()` and `getTotalUsers()`, the member count the member message carries, or the room's current viewer count for the social form. Every member message also publishes a `TikTokRoomInfoEvent` with that count. TikTok does not send a join for every viewer, so counting joins does not give the viewer count; read `getRoomInfo().getViewersCount()`. `TikTokJoinEvent.of(userName)` builds one for offline tests.

## TikTokLikeEvent

Triggered when a viewer sends likes to the streamer. For streams with many viewers, this event is not always triggered by TikTok. Handler: `onLike`. Messages: [`WebcastLikeMessage`](/schema/webcast/webcastlikemessage/), and [`WebcastSocialMessage`](/schema/webcast/webcastsocialmessage/) whose display text key is `pm_mt_msg_viewer`.

Key fields: `getUser()`; `getLikes()`, the likes in this update; and `getTotalLikes()`, the room's running total. From a like message both counts come from the message, and the client also updates `getRoomInfo().getLikesCount()` and publishes a `TikTokRoomInfoEvent`. From a social message `getLikes()` is `1` and `getTotalLikes()` is the room's count at that moment. TikTok batches likes, so add `getLikes()` and reconcile with `getTotalLikes()` rather than counting events. `TikTokLikeEvent.of(userName, likes)` builds one for offline tests.

```java
TikTokLive.newClient("isaackogz")
        .onLike((client, event) -> System.out.println(event.getUser().getProfileName() + " sent " + event.getLikes() + " likes, " + event.getTotalLikes() + " in total"))
        .buildAndConnect();
```

## TikTokFollowEvent

Triggers when a user follows the streamer. Based on social event. Handler: `onFollow`. Message: [`WebcastSocialMessage`](/schema/webcast/webcastsocialmessage/) whose display text key is `pm_main_follow_message_viewer_2`.

Key fields: `getUser()` and `getTotalFollowers()`, the follow count the message carries. `TikTokFollowEvent.of(userName)` builds one for offline tests.

## TikTokShareEvent

Triggers when a user shares the stream. Based on social event. Handler: `onShare`. Message: [`WebcastSocialMessage`](/schema/webcast/webcastsocialmessage/) whose display text key is `pm_mt_guidance_share`, or `pm_mt_guidance_viewer_<n>_share`, from which the client reads `n`.

Key fields: `getUser()` and `getTotalShares()`, `1` for the first key and `n` for the second.

## TikTokQuestionEvent

Triggered every time someone asks a new question via the question feature. Handler: `onQuestion`. Message: [`WebcastQuestionNewMessage`](/schema/webcast/webcastquestionnewmessage/).

Key fields: `getQuestionId()`, `getText()`, `getTime()` (the question's creation time), and `getUser()`.

## TikTokBarrageEvent

An animated banner notice, such as a high-grade viewer entering, a fan-level upgrade, or a subscription gift. Message: [`WebcastBarrageMessage`](/schema/webcast/webcastbarragemessage/), whose [`BarrageType`](/schema/enums/barragetype/) says which.

Key fields: `getEventName()`; `getDuration()`; `getIcon()`, `getBackGround()`, and `getRightIcon()`, the banner's pictures; `getBarrageParam()`, a `UserGradeParam` for a grade entrance notification, a `FansLevelParam` for a fan-level upgrade, a `SubscribeGiftParam` for a subscription gift, or a plain `BarrageParam` for every other type; and `getBarrageMessage()`, the whole message. The three parameter classes expose no getters in this version, so read the user and grade from `getBarrageMessage()`.

## TikTokCaptionEvent

Captions of the host's speech. Message: [`WebcastCaptionMessage`](/schema/webcast/webcastcaptionmessage/).

Key fields: `getCaptionTimeStamp()` and `getContents()`, a list of `CaptionContent` with `getISOLanguage()` and `getText()`, one per language the message carries.
