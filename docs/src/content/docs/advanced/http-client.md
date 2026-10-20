---
title: HTTP client and mappers
description: The requests TikTokLiveJava makes to TikTok and to the signing provider, how each request is built and retried, the response mappers, and the message-mapping layer that turns Webcast messages into events.
sidebar:
  order: 1
---

Two layers sit between the socket and your handlers. `LiveHttpClient`, implemented by `TikTokLiveHttpClient`, makes the HTTP requests: the creator's user data, the room information, the gift list, the signing request to [Euler Stream](https://www.eulerstream.com/), and the chat endpoint. `LiveMapper`, implemented by `TikTokLiveMapper`, turns each Webcast message into events. `connect()` drives both in a fixed order, described on the [connection page](/getting-started/connection/#the-connect-sequence); this page describes what they do so you can call them yourself or change what they produce.

## The requests

`TikTokLive.requests()` returns a `LiveHttpClient` with default settings, and `TikTokLive.requests(Consumer<LiveClientSettings>)` one configured the way a client is, so the requests go through your proxy, headers, and timeout; see [HTTP settings](/configuration/http-settings/#requests-outside-a-client). The interface has a `String` overload and a `Request` overload for each of the three TikTok requests.

| Method | Request | Returns |
| --- | --- | --- |
| `fetchLiveUserData(userName)` | `GET https://www.tiktok.com/api-live/user/room` with `uniqueId` and `sourceType=54`, plus the `sessionid` and `tt-target-idc` cookies when set | `LiveUserData.Response`: `getUserStatus()` (`Live`, `LivePaused`, `Offline`, `NotFound`), `getRoomInfo()` with the room id, title, start time, viewer counts, age restriction, and host, `isLiveOnline()`, `isHostNameValid()`, and `getJson()` |
| `fetchLiveData(roomId)` | `GET https://webcast.tiktok.com/webcast/room/info` with `room_id` and the same cookies | `LiveData.Response`: `getLiveStatus()` (`HostOnline`, `HostPaused`, `HostOffline`, `HostNotFound`), `isAgeRestricted()`, `getTitle()`, `getLikes()`, `getViewers()`, `getTotalViewers()`, `getHost()`, `getLiveType()` (`SOLO`, `BOX`, `CO_HOST`, `BATTLE`), and `getJson()` |
| `fetchRoomGiftsData(roomId)` | `GET https://webcast.tiktok.com/webcast/gift/list/` with `room_id`, no cookies | `GiftsData.Response`: `getGifts()`, a `List<Gift>` with id, name, diamond cost, picture, and the gift's JSON object, and `getJson()`; a body without `data.gifts` gives an empty list |
| `fetchLiveConnectionData(roomId)` | `GET https://tiktok.eulerstream.com/webcast/fetch` with `client=ttlive-java`, `room_id`, `session_id` when set, and the `x-api-key` header when set | `LiveConnectionData.Response`: `getWebsocketCookies()`, the `x-set-tt-cookie` header; `getWebsocketUrl()`, built from the response's push server, `room_id`, `cursor`, `resp_content_type=protobuf`, `internal_ext`, and the route parameters; `getWebcastResponse()`, the parsed `ProtoMessageFetchResult` |
| `sendChat(roomInfo, content, sessionId, ttTargetIdc)` | `POST https://tiktok.eulerstream.com/webcast/chat` with a JSON body of `content`, `sessionId`, `ttTargetIdc`, and `roomId`, and the `x-api-key` header when set | `true` when the provider answered 200; `false` otherwise, without an exception |

With `useEulerstreamEnterprise` the two provider requests go to `tiktok.enterprise.eulerstream.com` instead; see [signing](/configuration/signing/#enterprise-endpoints). Every request carries the parameters, headers, and cookies of the [HTTP settings](/configuration/http-settings/), so `app_language`, `aid`, the `User-Agent`, and the rest of TikTok's web-client parameters are on each URL and request.

```java
var requests = TikTokLive.requests(settings -> settings.getHttpSettings().setTimeout(Duration.ofSeconds(10)));
var userData = requests.fetchLiveUserData("isaackogz");
System.out.println("Status: " + userData.getUserStatus());

if (userData.getUserStatus() == LiveUserData.UserStatus.Live) {
    var roomId = userData.getRoomInfo().getRoomId();
    var liveData = requests.fetchLiveData(roomId);
    System.out.println(liveData.getTitle() + " (" + liveData.getLiveType() + "): " + liveData.getViewers() + " viewers");

    var gifts = requests.fetchRoomGiftsData(roomId);
    System.out.println(gifts.getGifts().size() + " gifts in the room's list");
}
```

### Errors

A TikTok request that answers with a status other than 200, or that fails in transport, throws `TikTokLiveRequestException`; the signing request throws `TikTokSignServerException`, a subclass, for a non-200 status, a response without the `x-set-tt-cookie` header, or a body that is not a `ProtoMessageFetchResult`. The message carries the status and a note chosen by the status code: `429` reads `Sign server rate limit reached`, with the wait computed from the `ratelimit-reset` header when present; `420` reads `IP Cloudflare Blocked`; `500` to `503` read `Sign server Error`; `504` reads `Sign server Timeout`. The notes are chosen in the shared `HttpClient`, so a 429 from TikTok's own API carries the same `Sign server` wording as one from the provider. The status table is on the [signing page](/configuration/signing/#rate-limits-and-provider-errors).

The response mappers add their own failures. `LiveUserDataMapper` throws `TikTokLiveRequestException` when TikTok answers `params_error`, returns `NotFound` with a `null` room info when it answers `user_not_found` or with no `data`, and, when the body is not the JSON it expects, logs `Malformed Json` at `WARNING` and returns `NotFound` as well; a captcha or block page therefore surfaces as an unknown host. `LiveDataMapper` throws `TikTokLiveRequestException` when the body is not a JSON object or has no `data` section. The [troubleshooting page](/troubleshooting/) lists every exception.

### Proxies

Each `fetch` method and `sendChat` wrap their request in a loop: while the proxy list is not empty, send through the next endpoint and, on `TikTokProxyRequestException`, try again; after the loop, send once without a proxy. The signing request also repeats on `TikTokSignServerException`. Inside one attempt, `HttpProxyClient` handles the endpoint selection, the discard of failed endpoints, and the fallback; with `type` `HTTP` or `DIRECT`, a response other than 200 moves on to the next endpoint without removing the current one, so a provider that keeps answering 429 through working proxies is retried in rotation until the list empties or the response changes. See [proxies and host](/configuration/proxies-and-host/#how-a-request-uses-the-list).

## How a request is built

`HttpClientFactory` holds the client's `LiveClientSettings` and the one `java.net.http.HttpClient` behind every request. `client(url)` returns an `HttpClientBuilder` over a `clone()` of the HTTP settings; `withParam`, `withParams`, `withHeader`, `withHeaders`, `withCookie` (which skips a `null` name or value), `withBody`, `withUrl`, and `withHttpClientSettings` edit that copy, and `build()` returns an `HttpProxyClient` when proxies are enabled and the list is not empty, otherwise an `HttpClient`.

`HttpClient.prepareRequest()` assembles the `HttpRequest`: `POST` when a body was given, the URL with the parameters URL-encoded and sorted as its query string (`toUri()`), the settings' `timeout` as the request timeout, the cookies joined into one `Cookie` header when the map is not empty, every header, and finally the `onRequestCreating` hook. `toHttpResponse(handler)` sends it and wraps the response in an `ActionResult`, a success for status 200 and a failure with the note above for anything else; `toJsonResponse()` and `toBinaryResponse()` are the `String` and `byte[]` forms. The JDK client itself is created once, on the first request: it follows redirects, keeps cookies in a `CookieManager`, uses `timeout` as its connect timeout, installs a `RotatingProxySelector` when proxies are enabled with a type other than `SOCKS`, and runs the `onClientCreating` hook. The selector binds the endpoint chosen for a request to the sending thread for the duration of that one `send`, which is how a single JDK client rotates proxies per request.

These classes are public in `io.github.jwdeveloper.tiktok.client.http`, so a request the client does not make can use the same machinery:

```java
import io.github.jwdeveloper.tiktok.client.http.HttpClientBuilder;

var settings = LiveClientSettings.createDefault().getHttpSettings();
var result = new HttpClientBuilder("https://www.tiktok.com/api-live/user/room", settings)
        .withParam("uniqueId", "isaackogz")
        .withParam("sourceType", "54")
        .build()
        .toJsonResponse();

if (result.isSuccess()) {
    System.out.println(result.getContent());
} else {
    System.out.println("Failed: " + result);
}
```

`new HttpClientBuilder(url)` without settings starts from an empty `HttpClientSettings`, with no parameters and no headers; `LiveClientSettings.createDefault().getHttpSettings()` gives the client's defaults.

## Response mappers

The JSON bodies are turned into the response objects by static methods in `io.github.jwdeveloper.tiktok.client.http.mappers`: `LiveUserDataMapper.map` reads the user, status, and `liveRoom` sections of the user request, `LiveDataMapper.map` reads the room information (its `getUser` builds the host as a `User` with the `LiveHost` attribute), and `GiftsDataMapper.mapRoom` reads the gift list, replacing a `.webp` picture URL with `.png`. `HttpRequestJsonMapper` and `HttpResponseJsonMapper` are Gson serializers for the JDK's `HttpRequest` and `HttpResponse`, for printing a request as JSON.

`LiveUserDataMapper.mapEulerstream` is the mapper of the Euler Stream WebSocket's `roomInfo` message. It fills the room id, title, start time, viewer count, and host, and reads the host's id, username, nickname, signature, avatar, and follower counts; it sets no total viewer count and no age restriction, and it writes the message's `totalViewers` into the viewer count as well. The [WebSocket page](/advanced/websocket/#the-euler-stream-socket) describes when it runs.

## The message mappers

Once the socket delivers a batch, `TikTokLiveMessageHandler` publishes `TikTokWebsocketResponseEvent` with the whole `ProtoMessageFetchResult` and handles each message in turn. A message whose `method` has no registered mapping is published as `TikTokWebsocketUnhandledMessageEvent`; a registered one goes through `LiveMapper.handleMapping(name, bytes)`, and each event it returns is published after a `TikTokWebsocketMessageEvent` that wraps it. An exception during one message is caught, wrapped in `TikTokLiveMessageException` with the message and the batch, and published as `TikTokErrorEvent`; the handler then continues with the next message. `publishMessage` on the client calls the same single-message path without a batch event and lets the exception propagate.

Three interfaces in `io.github.jwdeveloper.tiktok.api.mappers` make up the layer:

- `LiveMapper` keeps one `TikTokMapperModel` per message name. `forMessage(name)` or `forMessage(WebcastXMessage.class)` creates or returns the model, keyed by the class's simple name; the overloads with a `MappingAction` or a `Function<byte[], TikTokEvent>` set its mapping at the same time. `forAnyMessage()` returns the global model, `isRegistered` says whether a name has a model, and `handleMapping` runs the pipeline below.
- `TikTokMapperModel` has three hooks: `onBeforeMapping(MappingAction<byte[]>)` receives the bytes and returns the bytes to map; `onMapping(MappingAction<MappingResult>)` deserialises and builds the events; `onAfterMapping(Function<MappingResult, List<TikTokEvent>>)` receives the result and returns the events to publish. `getSourceMessageName()` is the key. A `MappingAction` receives the bytes, the message name, and the helper.
- `LiveMapperHelper` deserialises: `bytesToWebcastObject(bytes, WebcastXMessage.class)` and `bytesToWebcastObject(bytes, name)`, which looks the class up in `io.github.jwdeveloper.tiktok.api.messages.webcast`; `isMessageHasProtoClass(name)`; `bytesToProtoBufferStructure(bytes)`, which decodes any payload into a `ProtoBufferObject` without a schema; and `toJson(object)`.

`handleMapping` runs, in order, the message's `onBeforeMapping`, the global `onBeforeMapping`, the message's `onMapping`, the global `onMapping` only when the message's returned `null`, the message's `onAfterMapping`, and the global `onAfterMapping`. A model created by `forMessage(name)` alone maps to no events until `onMapping` is set, and the defaults of the hooks pass the bytes and the result through unchanged.

```java
TikTokLive.newClient("isaackogz")
        .mappings(mapper -> mapper.forAnyMessage().onBeforeMapping((bytes, messageName, helper) -> {
            System.out.println(messageName + ": " + bytes.length + " bytes");
            return bytes;
        }))
        .buildAndConnect();
```

The implementations live in `io.github.jwdeveloper.tiktok.client.mappers`. `MessagesMapperFactory.create` registers the default mappings: the control, poll, and pin messages go to `TikTokCommonEventHandler`, the live-intro, room-user, like, and member messages to `TikTokRoomInfoEventHandler`, the gift message to `TikTokGiftEventHandler`, the social message to `TikTokSocialMediaEventHandler`, and the chat, caption, subscription, emote, question, barrage, battle, and link messages are mapped inline, the link message by a `switch` on its type. The registrations of several other messages are commented out in the factory or absent, and that `switch` has no case for the apply and reply types, which is why the default client never produces the sixteen [unmapped events](/events/#unmapped-events). `mappings(mapper -> ...)` on the builder runs after the factory, so it can add to or replace any of them; see [custom events and mappings](/guides/custom-events/).

`TikTokLiveMapperHelper` is the helper; it throws `TikTokMessageMappingException` when a class has no `parseFrom` method or the bytes do not parse (the message reads `can't find parsing method` in both cases) and when a name has no class in the webcast package. `TikTokGenericEventMapper`, which it uses to look up `parseFrom`, also has a `mapToEvent(messageClass, eventClass, bytes)` method that finds the event constructor taking the message by reflection; nothing in this release calls it, so there is no name-based fallback from `WebcastXMessage` to `TikTokXEvent`.
