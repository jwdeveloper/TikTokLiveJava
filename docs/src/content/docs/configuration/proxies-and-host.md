---
title: Proxies and host
description: The hostName setting, and ProxyClientSettings, the proxy list, type, rotation, discard and fallback behaviour, and how the HTTP requests and the WebSocket use it.
sidebar:
  order: 5
---

## Host name

`hostName` is the creator's username, the part after `@` in `https://www.tiktok.com/@isaackogz/live`. `TikTokLive.newClient(hostName)` sets it, and `setHostName` in `configure` replaces it. `build()` removes a leading `@` and throws `TikTokLiveException` with the message `HostName can not be null` when the value is `null` or empty. The built client reports it as `getRoomInfo().getHostName()`, names its logger after it, and sends it as `uniqueId` to TikTok's user-data request and to the Euler Stream WebSocket.

```java
var client = TikTokLive.newClient("@isaackogz").build();
System.out.println(client.getRoomInfo().getHostName());
```

`TikTokLive.isHostNameValid(hostName)` asks TikTok whether a username exists; see [connection lifecycle](/getting-started/connection/#check-whether-a-creator-is-live).

## Proxies

`ProxyClientSettings` is the `proxyClientSettings` field of the [HTTP settings](/configuration/http-settings/). `configureProxy(Consumer<ProxyClientSettings>)` on `HttpClientSettings` sets `enabled` to `true` and runs your consumer; with `enabled` left `false`, the other fields have no effect.

```java
import java.net.Proxy;

TikTokLive.newClient("isaackogz")
        .configure(settings -> settings.getHttpSettings().configureProxy(proxy -> {
            proxy.setType(Proxy.Type.HTTP);
            proxy.addProxy("203.0.113.10:8080");
            proxy.addProxy("203.0.113.11", 8080);
            proxy.setRotation(ProxyClientSettings.Rotation.RANDOM);
            proxy.setOnProxyUpdated(proxyData ->
                    System.out.println("Using " + proxyData.getAddress() + ":" + proxyData.getPort()));
        }))
        .onComment((client, event) -> System.out.println(event.getText()))
        .buildAndConnect();
```

### Fields

| Field | Type | Default | Effect |
| --- | --- | --- | --- |
| `enabled` | `boolean` | `false` | Whether requests use the proxy list. `configureProxy` sets it. |
| `type` | `java.net.Proxy.Type` | `DIRECT` | `HTTP`, and `DIRECT` once proxies are enabled, send requests through the JDK HTTP client with the selected endpoint as an HTTP proxy. `SOCKS` opens an `HttpsURLConnection` through the endpoint as a SOCKS proxy; see [SOCKS and certificates](#socks-and-certificates). |
| `proxyList` | `List<ProxyData>` | Empty | The endpoints. Add them with `addProxy("host:port")`, `addProxy(host, port)`, `addProxy(InetSocketAddress)`, or `addProxies(List<String>)`. `getProxiesSnapshot()` returns a copy for iteration. |
| `rotation` | `Rotation` | `CONSECUTIVE` | How `next()` picks an endpoint: `CONSECUTIVE` walks the list in order and wraps, `RANDOM` picks any entry, `NONE` stays on `index`. |
| `index` | `int` | `0` | The current position in the list. `setIndex` throws `IndexOutOfBoundsException` for a value outside the list. |
| `autoDiscard` | `boolean` | `true` | Remove an endpoint from the list when a connection through it fails. |
| `fallback` | `boolean` | `true` | When a request through a proxy fails with an `IOException` whose message contains `503`, send it again without a proxy. |
| `allowWebsocket` | `boolean` | `true` | Whether the WebSocket also connects through the proxy list. |
| `onProxyUpdated` | `Consumer<ProxyData>` | No-op | Called with each endpoint `next()` selects. |

`ProxyData` holds an `address` and a `port` and nothing else; there is no field for proxy credentials. `addProxy("host:port")` parses with `ProxyData.map`, which throws `IllegalArgumentException` when the port is not an integer or the address does not resolve, and `IndexOutOfBoundsException` when the port is outside 0 to 65535.

### How a request uses the list

For each HTTP request, the client builds `HttpProxyClient` when proxies are enabled and the list is not empty, and the plain `HttpClient` otherwise. `HttpProxyClient` selects an endpoint with `next()` and sends the request through it:

- With `HTTP` or `DIRECT`, a response other than 200 moves on to the next endpoint; `hasNext()` is `true` while the list is not empty, so endpoints that answer with another status are tried again in rotation. A connect timeout or `ConnectException` removes the endpoint when `autoDiscard` is `true` and throws `TikTokProxyRequestException`; the client's `fetch` methods catch that and start the request again while endpoints remain, then make one last attempt without a proxy. Any other `IOException` throws it too, unless `fallback` applies.
- With `SOCKS`, one endpoint is tried per attempt; an `IOException` removes it when `autoDiscard` is `true` and throws `TikTokProxyRequestException`, with the same retry by the `fetch` methods.
- `TikTokLiveRequestException` with the message `No more proxies available!` is raised when the list runs empty inside one attempt. The `HTTP` and `DIRECT` path throws it; the `SOCKS` path catches it, prints its stack trace, and returns a failed result instead.

The signing request is repeated on `TikTokSignServerException` as well as on `TikTokProxyRequestException`, while endpoints remain and then once without a proxy; see [signing](/configuration/signing/).

### The WebSocket

When proxies are enabled and `allowWebsocket` is `true`, the socket to TikTok connects through the list: the client walks the endpoints with `next()`, sets each as the socket's `java.net.Proxy` of the configured `type`, and tries to connect; a failure removes the endpoint when `autoDiscard` is `true`. When the list runs empty, `connect()` throws `TikTokLiveException` with the message `Failed to connect to the websocket`. The Euler Stream WebSocket (`useEulerstreamWebsocket`) does not use the proxy settings; the HTTP requests of that client, such as `sendChat`, still do.

### SOCKS and certificates

With `type` set to `SOCKS`, both the HTTP requests and the WebSocket install an SSL context whose trust manager accepts every certificate, so the TLS connections made through a SOCKS proxy are not verified against the server's certificate chain. Use `HTTP` proxies when that matters.
