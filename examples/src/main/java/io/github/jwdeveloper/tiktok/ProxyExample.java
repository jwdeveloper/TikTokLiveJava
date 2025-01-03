/*
 * Copyright (c) 2023-2024 jwdeveloper jacekwoln@gmail.com
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

import java.net.Proxy;

public class ProxyExample {
    public static void main(String[] args) throws Exception {
        TikTokLive.newClient(ConnectionExample.TIKTOK_HOSTNAME)
            .configure(clientSettings -> {
                clientSettings.setPrintToConsole(true);
                clientSettings.getHttpSettings().configureProxy(proxySettings -> {
                    proxySettings.setOnProxyUpdated(proxyData -> System.err.println("Next proxy: " + proxyData.toString()));
                    proxySettings.setType(Proxy.Type.SOCKS);
                    proxySettings.addProxy("localhost", 8080);
                });
            })
            .onConnected((liveClient, event) ->
                liveClient.getLogger().info("Connected "+liveClient.getRoomInfo().getHostName()))
            .onComment((liveClient, event) -> liveClient.getLogger().info(event.getUser().getName()+": "+event.getText()))
            .onLike((liveClient, event) -> liveClient.getLogger().info(event.getUser().getName()+" sent "+event.getLikes()+"x likes!"))
            .onDisconnected((liveClient, event) ->
                liveClient.getLogger().info("Disconnect reason: "+event.getReason()))
            .onLiveEnded((liveClient, event) ->
                liveClient.getLogger().info("Live Ended: "+liveClient.getRoomInfo().getHostName()))
            .onError((liveClient, event) ->
                event.getException().printStackTrace())
            .buildAndConnect();

        System.in.read();
    }
}