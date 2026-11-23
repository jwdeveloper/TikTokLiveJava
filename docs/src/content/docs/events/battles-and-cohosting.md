---
title: Battles and cohosting
description: Battles and their points, the Power-Up card notice, and every link-mic event the client derives from WebcastLinkMessage, with the fields each one carries.
sidebar:
  order: 4
---

A battle is two or more hosts competing for gift points; link-mic is the signalling behind cohosting and multi-guest sessions. `TikTokLinkMicBattleEvent` and `TikTokLinkMicArmiesEvent` have their own messages. The `TikTokLink*` events share one message, [`WebcastLinkMessage`](/schema/webcast/webcastlinkmessage/): the client reads its [`LinkMessageType`](/schema/enums/linkmessagetype/) and builds the matching subclass of `TikTokLinkEvent`, or `TikTokLinkEvent` itself for a type with no subclass. A subclass that needs the message's content throws when it is absent, and the client publishes that as `TikTokErrorEvent`.

None of these events has a builder method; register them with `onEvent(TikTokXEvent.class, handler)`. Each section names the Webcast message and links its schema page. Replace `isaackogz` with a creator who is live.

## TikTokLinkMicBattleEvent

A battle started or finished, with its teams, hosts, and final scores. Message: [`WebcastLinkMicBattle`](/schema/webcast/webcastlinkmicbattle/).

Key fields: `getBattleId()`; `isFinished()`, `true` when the message's [`BattleAction`](/schema/enums/battleaction/) is `BATTLE_ACTION_FINISH`; `getBattleType()`, a [`BattleType`](/schema/enums/battletype/), with the helpers `is1v1()`, `is2v2()`, `isIndividual()` (1v1v1 or 1v1v1v1), and `isMultiTeam()` (1 against many); and `getTeams()`, a list of `Team` with `getTeamId()`, `getHosts()`, `getTotalPoints()` (`-1` until the battle has finished), `getWinStreak()` (the host's combo count in a 1v1 battle, otherwise `-1`), and `getViewers()`, the contributors and their points, filled when the battle has finished. `isTie()` compares the finished teams' points, and `getTeam(hostName)`, `getOpponentTeams(hostName)`, and `getTeams(hostName)` pick teams by a host's username. For a 1-against-many battle the teams list stays empty: the source notes that no data has been collected for it yet.

```java
TikTokLive.newClient("isaackogz")
        .onEvent(TikTokLinkMicBattleEvent.class, (client, event) -> {
            if (!event.isFinished()) {
                System.out.println("Battle " + event.getBattleId() + " started with " + event.getTeams().size() + " teams");
                return;
            }
            for (var team : event.getTeams()) {
                var hosts = team.getHosts().stream().map(host -> host.getName()).toList();
                System.out.println(hosts + ": " + team.getTotalPoints() + " points");
            }
        })
        .buildAndConnect();
```

## TikTokLinkMicArmiesEvent

A battle's points were updated, per host or team with the top contributors. Message: [`WebcastLinkMicArmies`](/schema/webcast/webcastlinkmicarmies/).

Key fields: `getBattleId()`; `isFinished()`, `true` when the message's [`TriggerReason`](/schema/enums/triggerreason/) is the battle's end; `getPicture()`, the animated icon; `getBattleType()`; and `getArmies()`, a map from the host's id in a 1v1 battle or the team's id otherwise to a `LinkMicArmy` with `getArmyId()`, `getTotalPoints()`, and `getArmies()`, the contributors as `User` objects mapped to their points. The map stays empty for a 1-against-many battle.

## TikTokLinkMicBattleItemCard

A battle Power-Up card notice; the event carries only the message header. Message: [`WebcastLinkMicBattleItemCard`](/schema/webcast/webcastlinkmicbattleitemcard/). The class name has no `Event` suffix.

## TikTokLinkMicFanTicketEvent

A fan-ticket notice of a link-mic session; the event carries only the message header. Message: [`WebcastLinkMicFanTicketMethod`](/schema/webcast/webcastlinkmicfanticketmethod/). Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).

## TikTokLinkMicMethodEvent

Link-mic signalling between hosts; the event carries only the message header. Message: [`WebcastLinkMicMethod`](/schema/webcast/webcastlinkmicmethod/). Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).

## TikTokLinkLayerEvent

A link layer change; the event carries only the message header. Message: [`WebcastLinkLayerMessage`](/schema/webcast/webcastlinklayermessage/). Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).

## TikTokLinkEvent

A link message of a type with no dedicated event, and the base class of the events below. Message: [`WebcastLinkMessage`](/schema/webcast/webcastlinkmessage/) with a `LinkMessageType` the mapping has no case for, among them `TYPE_LINKER_APPLY`, `TYPE_LINKER_REPLY`, `TYPE_LINKER_PERMIT`, and `TYPE_LINKER_LEAVE_V2`.

Key fields, inherited by every subclass: `getExtra()` and `getTransferExtra()`, the message's extra strings, and `getExpireTimestamp()`. A handler registered for `TikTokLinkEvent.class` receives only these generic events, not the subclasses.

## TikTokLinkAcceptNoticeEvent

An invitation was accepted. Message type `TYPE_LINKER_ACCEPT_NOTICE`.

Key fields: `getFromUserId()`, `getFromRoomId()`, and `getToUserId()`.

## TikTokLinkApplyEvent

A guest applied to join. Message type `TYPE_LINKER_APPLY`. Not produced by the default mapper, whose switch has no case for this type, so an application arrives as `TikTokLinkEvent`; see [unmapped events](/events/#unmapped-events). The class adds no fields.

## TikTokLinkCancelEvent

An invitation or application was cancelled. Message types `TYPE_LINKER_CANCEL_INVITE` and `TYPE_LINKER_CANCEL_APPLY`.

Key fields: `getFromUserId()`, `getToUserId()`, `getCancelType()`, and `getActionId()`.

## TikTokLinkCloseEvent

The link session was closed. Message type `TYPE_LINKER_CLOSE`. The class adds no fields.

## TikTokLinkCohostListChangeEvent

The cohost list changed. Message type `TYPE_LINKER_COHOST_LIST_CHANGE`. The class adds no fields.

## TikTokLinkCreateEvent

A link session was created. Message type `TYPE_LINKER_CREATE`.

Key fields: `getHostId()`, the owner; `getRoomId()`, the owner's room, which on this class replaces the header's room id; and `getLinkType()`.

## TikTokLinkEnterEvent

Users entered the link session, with the linked users list. Message type `TYPE_LINKER_ENTER`.

Key fields: `getListUsers()`, a list of `ListUser` with `getUser()`, `getLinkType()`, `getLinkMicId()`, `getLinkStatus()`, `getModifyTime()`, `getLinkerId()`, `getUserPosition()`, `getSilenceStatus()`, and `getRoleType()`; and `getAnchorMultiLiveEnum()`, a [`LinkmicMultiLiveEnum`](/schema/enums/linkmicmultiliveenum/).

## TikTokLinkInviteEvent

A host invited another host. Message type `TYPE_LINKER_INVITE`.

Key fields: `getInviter()`, a `User`, and `getRoomId()`, the inviter's room, which on this class replaces the header's room id.

## TikTokLinkKickOutEvent

A guest was removed, with the reason. Message type `TYPE_LINKER_KICK_OUT`.

Key fields: `getFromUserId()` and `getKickOutReason()`, the nested enum `TikTokLinkKickOutEvent.KickOutReason`: `KICKOUT_REASON_UNKNOWN`, `KICKOUT_REASON_FIRST_FRAME_TIMEOUT`, `KICKOUT_REASON_BY_HOST`, `KICKOUT_REASON_RTC_LOST_CONNECTION`, `KICKOUT_REASON_BY_PUNISH`, `KICKOUT_REASON_BY_ADMIN`, or `KICKOUT_REASON_HOST_REMOVE_ALL_GUESTS`, in the order of the schema's [`KickOutReason`](/schema/enums/kickoutreason/).

## TikTokLinkLeaveEvent

A guest left. Message type `TYPE_LINKER_LEAVE`.

Key fields: `getUserId()`, `getSendLeaveUid()`, `getLeaveReason()`, and `getLinkmicIdStr()`.

## TikTokLinkLinkedListChangeEvent

The linked users list changed, first version of the message. Message type `TYPE_LINKER_LINKED_LIST_CHANGE`. The class adds no fields; the second version, `TYPE_LINKER_LINKED_LIST_CHANGE_V2`, carries the lists and becomes `TikTokLinkListChangeEvent`.

## TikTokLinkListChangeEvent

The linked, applied, and connecting user lists changed. Message type `TYPE_LINKER_LINKED_LIST_CHANGE_V2`.

Key fields: `getLinkedUsers()`, `getAppliedUsers()`, and `getConnectingUsers()`, each a list of `ListUser` as on `TikTokLinkEnterEvent`.

## TikTokLinkMediaChangeEvent

A guest's microphone or camera was opened or closed. Message type `TYPE_LINKER_MEDIA_CHANGE`.

Key fields: `getOp()`, a [`GuestMicCameraManageOp`](/schema/enums/guestmiccameramanageop/); `getToUserId()`; `getAnchorId()`; `getRoomId()`, which on this class replaces the header's room id; and `getChangeScene()`, a [`GuestMicCameraChangeScene`](/schema/enums/guestmiccamerachangescene/).

## TikTokLinkMicIdxUpdateEvent

A guest's microphone position changed. Message type `TYPE_LINKER_MIC_IDX_UPDATE`. The class adds no fields.

## TikTokLinkMuteEvent

A guest was muted or unmuted. Message type `TYPE_LINKER_MUTE`.

Key fields: `getUserId()`, `getStatus()`, a [`MuteStatus`](/schema/enums/mutestatus/), and `isMuted()`.

## TikTokLinkRandomMatchEvent

A random match paired the host with another host. Message type `TYPE_LINKER_MATCH`.

Key fields: `getUser()`, `getRoomId()` (the matched room, which on this class replaces the header's room id), `getInviteType()`, `getMatchId()`, and `getInnerChannelId()`.

## TikTokLinkReplyEvent

A host replied to an invitation. Message type `TYPE_LINKER_REPLY`. Not produced by the default mapper, whose switch has no case for this type, so a reply arrives as `TikTokLinkEvent`; see [unmapped events](/events/#unmapped-events).

Key fields: `getRoomId()`, `getFromUser()`, and `getToUser()`.

## TikTokLinkSysKickOutEvent

The system removed a guest. Message type `TYPE_LINKER_SYS_KICK_OUT`.

Key fields: `getUserId()` and `getLinkmicIdStr()`.

## TikTokLinkUpdateUserEvent

A linked user's state was updated. Message type `TYPE_LINKER_UPDATE_USER`.

Key fields: `getFromUserId()` and `getToUserId()`.

## TikTokLinkUpdateUserSettingEvent

A linked user's settings were updated. Message type `TYPE_LINKER_UPDATE_USER_SETTING`. The class adds no fields.

## TikTokLinkUserToastEvent

A toast message about a linked user. Message type `TYPE_LINKMIC_USER_TOAST`.

Key fields: `getUserId()`; `getRoomId()`, which on this class replaces the header's room id; and `getDisplayText()`, a `Text` with `getKey()`, `getPattern()`, `getValue()` (the pattern with its pieces filled in), and `getTextPieces()`.

## TikTokLinkWaitListChangeEvent

The waiting list changed. Message types `TYPE_LINKER_WAITING_LIST_CHANGE` and `TYPE_LINKER_WAITING_LIST_CHANGE_V2`. The class adds no fields.
