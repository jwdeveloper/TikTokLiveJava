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
package io.github.jwdeveloper.tiktok.client.websocket;

import io.github.jwdeveloper.tiktok.client.common.AsyncHandler;
import org.java_websocket.WebSocket;

import java.util.Base64;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class WebSocketHeartbeatTask
{
    private ScheduledFuture<?> task;
    private WebSocket webSocket;

    private final static byte[] heartbeatBytes = Base64.getDecoder().decode("MgJwYjoCaGI="); // Used to be '3A026862' aka ':\x02hb', now is '2\x02pb:\x02hb'.

    public synchronized void run(WebSocket webSocket, long pingTaskTime) {
        stop();
        if (!webSocket.isOpen()) return;
        this.webSocket = webSocket;
        // The first tick uses this monitor too, so it cannot run before task is assigned.
        task = AsyncHandler.getHeartBeatScheduler().scheduleAtFixedRate(() -> sendHeartbeat(webSocket), 0, pingTaskTime, TimeUnit.MILLISECONDS);
    }

    private synchronized void sendHeartbeat(WebSocket socket) {
		if (webSocket == socket && task != null)
			try {
				if (socket.isOpen())
					socket.send(heartbeatBytes);
				else
					stop();
			} catch (Exception e) {
				stop();
				e.printStackTrace();
			}
	}

    public synchronized void stop() {
        if (task != null)
            task.cancel(false);
        task = null;
        webSocket = null;
    }

    synchronized void stop(WebSocket socket) {
        if (webSocket == socket)
            stop();
    }
}