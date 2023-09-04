package io.github.jwdeveloper.tiktok.websocket;

import org.java_websocket.WebSocket;
import java.util.Random;

public class TikTokWebSocketPingingTask
{
    private Thread thread;

    private boolean isRunning = false;
    private final int MIN_TIMEOUT = 5;
    private final int MAX_TIMEOUT = 100;


    public void run(WebSocket webSocket)
    {
        var thread = new Thread(() ->
                {
                    pingTask(webSocket);
                });
        isRunning =true;
        thread.start();
    }

    public void stop()
    {
        if(thread != null)
        {
            thread.interrupt();
        }
        isRunning = false;
    }


    private void pingTask(WebSocket webSocket)
    {
        var random = new Random();
        while (isRunning)
        {
            try
            {
                if(!webSocket.isOpen())
                {
                    Thread.sleep(100);
                    continue;
                }
                webSocket.sendPing();
                var timeout = random.nextInt(MAX_TIMEOUT)+MIN_TIMEOUT;
                Thread.sleep(timeout);
            }
            catch (Exception e)
            {
                isRunning = false;
            }
        }

    }
}
