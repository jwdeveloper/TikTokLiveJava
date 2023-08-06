package io.github.jwdeveloper.tiktok.websocket;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;


public  class TikTokWebSocketListener implements java.net.http.WebSocket.Listener {

    @Override
    public void onOpen(java.net.http.WebSocket webSocket) {
        System.out.println("WebSocket opened");
    }

    @Override
    public void onError(java.net.http.WebSocket webSocket, Throwable error) {
        System.out.println("Error occurred: " + error.getMessage());
    }

    @Override
    public CompletionStage<?> onText(java.net.http.WebSocket webSocket, CharSequence data, boolean last) {
        System.out.println("Received onText: " + data);
        return java.net.http.WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onPing(WebSocket webSocket, ByteBuffer message) {
        System.out.println("Received onPing: " );
        return java.net.http.WebSocket.Listener.super.onPing(webSocket, message);
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        System.out.println("Received onBinary: " + data);
        return java.net.http.WebSocket.Listener.super.onBinary(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
        System.out.println("WebSocket closed with status code: " + statusCode + " and reason: " + reason);
        return java.net.http.WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    @Override
    public CompletionStage<?> onPong(WebSocket webSocket, ByteBuffer message) {
        System.out.println("Received onPong: ");
        return java.net.http.WebSocket.Listener.super.onPong(webSocket,message);
    }
}