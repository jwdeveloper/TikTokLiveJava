package io.github.jwdeveloper.tiktok.websocket;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Map;

public class Client2 extends WebSocketClient {
    public Client2(URI serverUri, Map<String, String> httpHeaders) {
        super(serverUri,httpHeaders);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {

        System.out.println("Open");
    }

    @Override
    public void onMessage(String s) {
        System.out.println("MESSAGE");
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        System.out.println("MESSAGE binary");
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("Close");
    }

    @Override
    public void onError(Exception e) {
        System.out.println("Error");
    }
}
