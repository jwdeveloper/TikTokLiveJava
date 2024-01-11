/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok;

import java.net.*;
import java.net.http.*;
import java.util.*;
import java.util.stream.Stream;

public class ProxyExample
{
    public static void main(String[] args) throws Exception {
        // TikTokLive.newClient(SimpleExample.TIKTOK_HOSTNAME)

        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.proxyscrape.com/v2/?request=displayproxies&protocol=socks4,socks5&timeout=10000&country=us")).GET().build();
        HttpResponse<Stream<String>> response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofLines());

        List<AbstractMap.SimpleEntry<String, Integer>> entries = new ArrayList<>(response.body().map(s -> {
            String[] split = s.split(":");
            return new AbstractMap.SimpleEntry<>(split[0], Integer.parseInt(split[1]));
        }).toList());

        TikTokLive.newClient("dash4214")
            .configure(clientSettings ->
            {
                clientSettings.setPrintToConsole(true);
                clientSettings.getHttpSettings().configureProxy(proxySettings -> {
                    proxySettings.setOnProxyUpdated(proxyData ->
                    {
                        System.err.println("Next proxy: "+proxyData.toString());
                    });
                    proxySettings.setType(Proxy.Type.SOCKS);
                    entries.forEach(entry -> proxySettings.addProxy(entry.getKey(), entry.getValue()));
                });
            })
            .onComment((liveClient, event) -> {
                liveClient.getLogger().info(event.getUser().getName()+": "+event.getText());
            })
            .onConnected((liveClient, event) ->
            {
                liveClient.getLogger().info("Hello world!");
            })
            .onDisconnected((liveClient, event) ->
            {
                liveClient.getLogger().info("Goodbye world!");
            })
            .onError((liveClient, event) ->
            {
                event.getException().printStackTrace();
            })
            .buildAndConnect();

        System.in.read();
    }
}