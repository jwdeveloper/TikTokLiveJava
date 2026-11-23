---
title: Gifts and goals
description: TikTokGiftEvent and TikTokGiftComboEvent with the streak rules, goal updates, envelopes, and treasure chests.
sidebar:
  order: 3
---

A sent gift arrives as `TikTokGiftEvent`, and every frame of a streak as `TikTokGiftComboEvent`. The two classes share the gift, sender, recipient, and streak count; the combo event adds the streak state. The other three events on this page describe goals and treasure chests and are not produced by the default mapper. The [gifts guide](/guides/gifts/) covers counting streaked gifts and the gift list.

The `Gift` on both gift events has `getId()`, `getName()`, `getDiamondCost()`, `getPicture()`, `getProperties()` (a `JsonObject` of extra properties, empty for a gift built from a message), and the helpers `hasDiamondCost(int)` and `hasDiamondCostRange(int, int)`. The client resolves it from `getGiftManager()` by id, then by name, and otherwise builds it from the message and attaches it to the manager; with `fetchGifts` set to `true`, the manager knows the room's gifts before the first event.

Each section names the Webcast message and links its schema page, which lists every field. Replace `isaackogz` with a creator who is live.

## TikTokGiftEvent

Triggered when a user sends a gift that has no combo (most of the expensive gifts), or when a combo has finished. Handler: `onGift`. Message: [`WebcastGiftMessage`](/schema/webcast/webcastgiftmessage/).

Key fields: `getGift()`; `getUser()`, the sender, with the identity attributes such as `isSubscriber()`; `getToUser()`, the recipient, which is the room's host when the message names none; and `getCombo()`, the message's combo count, the number of gifts in the streak. For a gift whose type is not streakable the event is published at once. For a streakable gift it is published once, when TikTok sends the streak's final frame or, when no final frame arrives, at the first gift message more than five minutes after the streak's last update, so one `TikTokGiftEvent` is one completed streak and `getCombo()` times `getDiamondCost()` is its value. A final frame TikTok repeats within a minute is dropped. `TikTokGiftEvent.of(gift)` and `of(name, id, diamonds)` build one for offline tests.

```java
TikTokLive.newClient("isaackogz")
        .onGift((client, event) -> {
            var gift = event.getGift();
            var diamonds = gift.getDiamondCost() * event.getCombo();
            System.out.println(event.getUser().getProfileName() + " sent " + event.getCombo() + " x " + gift.getName() + " (" + diamonds + " diamonds)");
        })
        .buildAndConnect();
```

## TikTokGiftComboEvent

Triggered every time a gift is sent, with the streak's state. Handler: `onGiftCombo`. Message: [`WebcastGiftMessage`](/schema/webcast/webcastgiftmessage/).

Key fields: the four of `TikTokGiftEvent`, and `getComboState()`, a `GiftComboStateType`: `Begin` on the first frame of a streak, `Active` on each further frame while the viewer holds the button, and `Finished` on the final frame, on a non-streakable gift, and when a streak times out. A `Finished` combo event is followed by the `TikTokGiftEvent` for the same gift, so count gifts in one handler or the other. Although the class extends `TikTokGiftEvent`, `onGift` is not called for it: handlers match the exact class. `TikTokGiftComboEvent.of(gift, combo, state)` builds one for offline tests.

```java
TikTokLive.newClient("isaackogz")
        .onGiftCombo((client, event) -> {
            switch (event.getComboState()) {
                case Begin -> System.out.println(event.getUser().getProfileName() + " started a " + event.getGift().getName() + " streak");
                case Active -> System.out.println("Streak at " + event.getCombo());
                case Finished -> System.out.println("Streak ended at " + event.getCombo());
            }
        })
        .buildAndConnect();
```

## TikTokGoalUpdateEvent

A goal's progress was updated. Message: [`WebcastGoalUpdateMessage`](/schema/webcast/webcastgoalupdatemessage/). Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).

Key fields: `getGoalId()`, `getDescription()`, `getPicture()` (the contributor's avatar), and `getUsers()`, the goal's contributors as `User` objects with id, display id, and avatar.

## TikTokEnvelopeEvent

An envelope, the sender of a treasure chest. Message: [`WebcastEnvelopeMessage`](/schema/webcast/webcastenvelopemessage/). Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).

Key field: `getUser()`, built from the envelope's sender name and avatar; its id is `0`.

## TikTokChestEvent

A treasure chest with its diamond total and participant count. Message: [`WebcastEnvelopeMessage`](/schema/webcast/webcastenvelopemessage/). Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).

Key fields: `getChest()`, a `Chest` with `getTotalDiamonds()` and `getTotalUsers()`; `getUser()`, the sender taken from the message's display text; and `getOpenedAt()`, a `Date` from the envelope's unpack time. The client has a handler for it, `TikTokCommonEventHandler.handleEnvelop`, which publishes the event only for an envelope whose [`EnvelopeDisplay`](/schema/enums/envelopedisplay/) is `ENVELOPE_DISPLAY_NEW`, but the factory does not register it; a custom mapping can call it.
