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
package io.github.jwdeveloper.tiktok.websocket;

import org.java_websocket.WebSocket;

import java.util.*;
import java.util.concurrent.*;

public class WebSocketHeartbeatTask
{
    // Single shared pool for all heartbeat tasks
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, r -> {
        Thread t = new Thread(r, "heartbeat-pool");
        t.setDaemon(true);
        return t;
    });
    private static final Map<WebSocket, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    private static final Map<WebSocket, Long> commTime = new ConcurrentHashMap<>();

    private final byte[] heartbeatBytes = Base64.getDecoder().decode("MgJwYjoCaGI="); // Used to be '3A026862' aka ':\x02hb', now is '2\x02pb:\x02hb'.

    public void run(WebSocket webSocket, long pingTaskTime) {
        stop(webSocket); // remove existing task if any

        tasks.put(webSocket, scheduler.scheduleAtFixedRate(() -> {
            try {
                if (webSocket.isOpen()) {
                    webSocket.send(heartbeatBytes);
                    commTime.put(webSocket, System.currentTimeMillis());
                } else {
                    Long time = commTime.get(webSocket);
                    if (time != null && System.currentTimeMillis() - time >= 60_000) // Stop if disconnected longer than 60s
                        stop(webSocket);
                }
            } catch (Exception e) {
                e.printStackTrace();
                stop(webSocket);
            }
        }, 0, pingTaskTime, TimeUnit.MILLISECONDS));
    }

    public void stop(WebSocket webSocket) {
        ScheduledFuture<?> future = tasks.remove(webSocket);
        if (future != null)
			future.cancel(true);
        commTime.remove(webSocket);
    }

    public void shutdown() {
        tasks.values().forEach(f -> f.cancel(true));
        commTime.clear();
        scheduler.shutdownNow();
    }
}