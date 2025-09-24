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

import io.github.jwdeveloper.tiktok.common.AsyncHandler;
import org.java_websocket.WebSocket;

import java.util.*;
import java.util.concurrent.*;

public class WebSocketHeartbeatTask
{
    private ScheduledFuture<?> task;
    private Long commTime;

    private final static byte[] heartbeatBytes = Base64.getDecoder().decode("MgJwYjoCaGI="); // Used to be '3A026862' aka ':\x02hb', now is '2\x02pb:\x02hb'.

    public void run(WebSocket webSocket, long pingTaskTime) {
        stop(); // remove existing task if any

        task = AsyncHandler.getHeartBeatScheduler().scheduleAtFixedRate(() -> {
            try {
                if (webSocket.isOpen()) {
                    webSocket.send(heartbeatBytes);
                    commTime = System.currentTimeMillis();
                } else if (commTime != null && System.currentTimeMillis() - commTime >= 60_000) // Stop if disconnected longer than 60s
					stop();
            } catch (Exception e) {
                e.printStackTrace();
                stop();
            }
        }, 0, pingTaskTime, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (task != null)
            task.cancel(true);
    }
}