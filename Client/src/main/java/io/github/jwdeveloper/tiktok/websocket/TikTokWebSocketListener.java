package io.github.jwdeveloper.tiktok.websocket;

import java.util.concurrent.CompletionStage;


public  class TikTokWebSocketListener implements java.net.http.WebSocket.Listener {
    //Insert Body here

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
        System.out.println("Received message: " + data);
        return java.net.http.WebSocket.Listener.super.onText(webSocket, data, last);
    }


    @Override
    public CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
        System.out.println("WebSocket closed with status code: " + statusCode + " and reason: " + reason);
        return java.net.http.WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }
}