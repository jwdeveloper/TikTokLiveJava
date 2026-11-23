---
title: Games and commerce
description: The live shopping event, which exists in the API but needs a custom mapping to be produced.
sidebar:
  order: 9
---

One event describes live shopping. The default mapper does not register its message, and the class's own source marks its constructor as not working, so treat it as a starting point for a custom mapping rather than a finished feature. Register it with `onEvent(TikTokShopEvent.class, handler)`.

## TikTokShopEvent

A product shown in live shopping. Message: [`WebcastOecLiveShoppingMessage`](/schema/webcast/webcastoecliveshoppingmessage/). Not produced by the default mapper; see [unmapped events](/events/#unmapped-events).

Key fields, read from the message's shop data: `getTitle()`, `getPrice()` (the price string), `getPicture()`, `getShopUrl()`, and `getShopName()`.
