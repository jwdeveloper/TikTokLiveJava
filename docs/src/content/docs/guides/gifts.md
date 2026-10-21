---
title: Gifts and combos
description: Count a streaked gift once with TikTokGiftEvent, follow a streak with TikTokGiftComboEvent, read the room's gift list from the gift manager, and test gift handlers offline.
sidebar:
  order: 1
---

A gift reaches your code as two events. `TikTokGiftEvent` is published once per gift: at once for a gift that cannot streak, and once per completed streak for one that can. `TikTokGiftComboEvent` is published for every frame of a streak, with the streak's state. Both carry `getGift()`, `getUser()` (the sender), `getToUser()` (the recipient, which is the host when the message names none), and `getCombo()`, the streak's count; the field lists are on the [gifts and goals page](/events/gifts-and-goals/). This guide is based on the repository's `Events_And_Gifts_Testing_Example.java` and the README's `onGift` and `onGiftCombo` sections. Replace `isaackogz` with a creator who is live.

## Count a gift once

A viewer can hold the gift button to send the same gift repeatedly. TikTok reports the streak as a series of `WebcastGiftMessage`s whose combo count climbs, then a final frame with the total. The client's gift handler keeps the state of each streak, keyed by sender, gift, and group, and publishes `TikTokGiftEvent` only when the streak is complete: on the final frame or, when TikTok sends none, at the first gift message that arrives more than five minutes after the streak's last frame. A final frame that TikTok repeats within a minute is dropped. A gift whose type cannot streak is published at once. So `onGift` sees each gift exactly once, and `getCombo()` times `getDiamondCost()` is the value of the whole streak.

```java
TikTokLive.newClient("isaackogz")
        .onGift((client, event) -> {
            var gift = event.getGift();
            var diamonds = (long) gift.getDiamondCost() * event.getCombo();
            System.out.println(event.getUser().getProfileName() + " sent " + event.getCombo() + " x " + gift.getName()
                    + " (" + diamonds + " diamonds) to " + event.getToUser().getProfileName());
        })
        .buildAndConnect();
```

Handlers are matched by the event's exact class, so `onGift` is never called for a `TikTokGiftComboEvent`, although that class extends `TikTokGiftEvent`; a `Finished` combo event and the `TikTokGiftEvent` for the same gift are two separate publications, in that order. The diamond cost is TikTok's price metadata for the gift, not what the creator is paid.

## Follow a streak while it runs

`onGiftCombo` receives every frame. `getComboState()` is a `GiftComboStateType`: `Begin` on the first frame of a streak, `Active` on each later frame, and `Finished` on the final frame, on a non-streakable gift, and when a streak times out. `getCombo()` is the count so far and is cumulative, so display the latest value rather than adding the frames up. Count gifts in `onGift`, not in the `Finished` branch here: a streakable gift whose first frame is already its final one produces no combo frame at all, so counting here misses it.

```java
TikTokLive.newClient("isaackogz")
        .onGiftCombo((client, event) -> {
            var who = event.getUser().getProfileName();
            var what = event.getGift().getName();
            switch (event.getComboState()) {
                case Begin -> System.out.println(who + " started a " + what + " streak");
                case Active -> System.out.println(who + ": " + event.getCombo() + " x " + what + " so far");
                case Finished -> System.out.println(who + " finished at " + event.getCombo() + " x " + what);
            }
        })
        .buildAndConnect();
```

A streakable gift produces `Begin`, zero or more `Active` frames, then `Finished`, but the handler publishes that `Finished` frame only when a `Begin` preceded it: a streakable gift whose first frame is already its final one produces no combo frame at all, only its `TikTokGiftEvent`. A non-streakable gift produces a single `Finished` frame, followed by its `TikTokGiftEvent`. Whether a gift can streak is the gift type in the message, which the handler reads before it builds the events; the `Gift` object has no field for it, so tell the two kinds apart by whether a `Begin` frame arrived.

## The gift list

`client.getGiftManager()` returns the `GiftsManager`, the client's index of known gifts by id: `getById(int)`, `getByName(String)` (case-insensitive), `getByFilter(Predicate<Gift>)`, `getManyByFilter(Predicate<Gift>)`, `toList()`, and `toMap()`. A lookup that finds nothing returns `Gift.UNDEFINED`, a gift with id `-1`, name `undefined`, and cost `-1`, rather than `null`. `attachGift(Gift)` and `attachGiftsList(List<Gift>)` add your own entries.

The manager starts empty. With `fetchGifts` set to `true`, `connect()` downloads the room's gift list after `TikTokPreConnectionEvent` and before the socket opens, so every gift is known before the first event; see [client settings](/configuration/client-settings/). Otherwise the handler resolves each gift message by id, then by name, and when neither is known it builds the `Gift` from the message and attaches it, so the manager fills up as gifts arrive.

A `Gift` has `getId()`, `getName()`, `getDiamondCost()`, `getPicture()` (a `Picture` with `getLink()` and `downloadImage()`), `getProperties()` (a `JsonObject` with the extra fields of the fetched list, empty for a gift built from a message), and the helpers `hasDiamondCost(int)` and `hasDiamondCostRange(int, int)`.

```java
TikTokLive.newClient("isaackogz")
        .configure(settings -> settings.setFetchGifts(true))
        .onConnected((client, event) -> {
            var gifts = client.getGiftManager();
            System.out.println(gifts.toList().size() + " gifts in this room");
            for (var gift : gifts.getManyByFilter(g -> g.getDiamondCost() >= 1000)) {
                System.out.println(gift.getName() + ": " + gift.getDiamondCost() + " diamonds, " + gift.getPicture().getLink());
            }
        })
        .buildAndConnect();
```

## Test gift handlers offline

`TikTokGiftEvent.of(gift)`, `TikTokGiftEvent.of(name, id, diamonds)`, and `TikTokGiftComboEvent.of(gift, combo, state)` build events without a message, and `publishEvent` dispatches them to your handlers without a connection. In offline mode nothing fetches the gift list, so `getGiftManager().getByName(...)` returns `Gift.UNDEFINED` until you attach the gifts the test needs; the repository's example looks `Rose` up without attaching it and so publishes the undefined gift. See [offline testing](/guides/offline-testing/) for what the mode can and cannot do.

```java
var client = TikTokLive.newClient("isaackogz")
        .configure(settings -> settings.setOffline(true))
        .onGift((liveClient, event) ->
                System.out.println(event.getCombo() + " x " + event.getGift().getName() + " from " + event.getUser().getName()))
        .onGiftCombo((liveClient, event) ->
                System.out.println(event.getComboState() + " at " + event.getCombo()))
        .build();

var rose = new Gift(5655, "Rose", 1, "");
client.getGiftManager().attachGift(rose);

client.publishEvent(TikTokGiftComboEvent.of(rose, 1, GiftComboStateType.Begin));
client.publishEvent(TikTokGiftComboEvent.of(rose, 12, GiftComboStateType.Finished));
client.publishEvent(TikTokGiftEvent.of(client.getGiftManager().getByName("rose")));
client.publishEvent(TikTokGiftEvent.of("Galaxy", 7122, 1000));
```
