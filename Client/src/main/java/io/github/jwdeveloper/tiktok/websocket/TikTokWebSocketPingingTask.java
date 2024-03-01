package io.github.jwdeveloper.tiktok.websocket;

import org.java_websocket.WebSocket;

import java.util.Random;

public class TikTokWebSocketPingingTask
{
    private Thread thread;
    private boolean isRunning = false;
    private final int MAX_TIMEOUT = 250;
    private final int SLEEP_TIME = 500;

    public void run(WebSocket webSocket, long pingTaskTime)
    {
        stop();
        thread = new Thread(() -> pingTask(webSocket, pingTaskTime));
        isRunning = true;
        thread.start();
    }

    public void stop()
    {
        if (thread != null)
            thread.interrupt();
        isRunning = false;
    }

    private void pingTask(WebSocket webSocket, long pingTaskTime)
    {
        var random = new Random();
        while (isRunning) {
            try {
                if (!webSocket.isOpen()) {
                    Thread.sleep(SLEEP_TIME);
                    continue;
                }
                webSocket.sendPing();

                Thread.sleep(pingTaskTime+random.nextInt(MAX_TIMEOUT));
            }
            catch (Exception e) {
                isRunning = false;
            }
        }

    }
}