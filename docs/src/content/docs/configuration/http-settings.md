---
title: HTTP settings
description: Every field of HttpClientSettings, request parameters, headers, cookies, timeout, proxy settings, and the hooks on the HTTP transport and on each request.
sidebar:
  order: 2
---

`HttpClientSettings` is the `httpSettings` field of `LiveClientSettings`, reached in `configure` with `settings.getHttpSettings()`. It shapes every HTTP request the client sends, the user, room, and gift requests to TikTok and the signing and chat requests to the provider, and it supplies the headers and connect timeout of the WebSocket handshake.

Each request starts from a `clone()` of the settings, so the values you set are the baseline for every request and nothing one request adds to the maps leaks into the next. The `java.net.http.HttpClient` behind the requests is created once per client, on its first request, from the settings at that moment.

```java
TikTokLive.newClient("isaackogz")
        .configure(settings -> {
            var http = settings.getHttpSettings();
            http.setTimeout(Duration.ofSeconds(10));
            http.getHeaders().put("Accept-Language", "fr-FR,fr;q=0.9");
            http.getParams().put("tz_name", "Europe/Paris");
            http.onRequestCreating(request -> request.header("X-Request-Source", "my-bot"));
        })
        .buildAndConnect();
```

## Fields

| Field | Type | Default | Effect |
| --- | --- | --- | --- |
| `params` | `Map<String, Object>` | `LiveClientSettings.DefaultClientParams()`: `aid`, `app_name`, `browser_name`, `browser_version`, `device_platform`, `screen_width`, `tz_name`, and the other parameters of TikTok's web client | Appended to every request URL as its URL-encoded query string. The map is a `TreeMap`, so the parameters are sent sorted by name. `build()` sets `app_language` and `webcast_language` from `clientLanguage`. |
| `headers` | `Map<String, String>` | `LiveClientSettings.DefaultRequestHeaders()`: `User-Agent`, `Referer`, `Origin`, `Accept`, `Accept-Language`, `Cache-Control`, `authority` | Set on every request, and sent in the WebSocket handshake on both the TikTok socket and the Euler Stream socket. |
| `cookies` | `Map<String, String>` | Empty | Joined into one `Cookie` header, `name=value; name=value`, when the map is not empty. The client adds `sessionid` and `tt-target-idc` from `LiveClientSettings` to TikTok's user and room requests. The WebSocket handshake sends the cookies from the signing response, not this map. |
| `proxyClientSettings` | `ProxyClientSettings` | Disabled, no proxies | Proxy list, type, rotation, and failure handling; `configureProxy` enables it. See [proxies and host](/configuration/proxies-and-host/). |
| `onClientCreating` | `Consumer<HttpClient.Builder>` | No-op | Called once, when the client's `java.net.http.HttpClient` is created on the first request, after redirect following, a `CookieManager`, the connect timeout, and the proxy selector are set. Set it with `onClientCreating(Consumer)`. |
| `onRequestCreating` | `Consumer<HttpRequest.Builder>` | No-op | Called for every request after the URI, timeout, and headers are set and before the request is built. Set it with `onRequestCreating(Consumer)`. |
| `timeout` | `Duration` | 3 seconds | The connect timeout of the HTTP transport and the timeout of each request. The constructor's own default is 2 seconds; `createDefault()` sets 3. See [the timeout and sub-second parts](#the-timeout-and-sub-second-parts). |

`params`, `headers`, and `cookies` are final maps with getters only: change their entries with `put` and `remove`. `proxyClientSettings` and `timeout` have setters; the two hooks are set through the methods named after them.

## The timeout and sub-second parts

The HTTP transport and each request receive the whole `Duration`. The WebSocket handshake, and the SOCKS proxy path, receive `timeout.toMillisPart()`, the millisecond fraction of the duration (0 to 999), as a connect timeout in milliseconds, and the SOCKS path uses the same value as its read timeout. With a whole-second value such as the default that is `0`, which `Socket.connect` treats as no timeout. `Duration.ofMillis(2500)` gives the HTTP requests 2.5 seconds and the WebSocket handshake 500 milliseconds.

## Default parameters and headers

The defaults present the client as TikTok's web client in Chrome 102 on Windows: the `User-Agent` header and the `browser_version` parameter carry that string, `aid` is `1988`, `app_name` is `tiktok_web`, and `tz_name` is `Europe/Berlin`. `DefaultClientParams()` and `DefaultRequestHeaders()` are public static methods of `LiveClientSettings`, so you can print the full lists:

```java
LiveClientSettings.DefaultClientParams().forEach((name, value) -> System.out.println(name + "=" + value));
LiveClientSettings.DefaultRequestHeaders().forEach((name, value) -> System.out.println(name + ": " + value));
```

## Requests outside a client

`TikTokLive.requests(Consumer<LiveClientSettings>)` returns the `LiveHttpClient` the static checks use, configured with the same settings, so a live check can go through your proxy or carry your headers. `TikTokLive.isLiveOnline` and `isHostNameValid` use the defaults.

```java
var requests = TikTokLive.requests(settings -> settings.getHttpSettings().setTimeout(Duration.ofSeconds(10)));
var userData = requests.fetchLiveUserData(new LiveUserData.Request("isaackogz"));
System.out.println(userData.isLiveOnline());
```
