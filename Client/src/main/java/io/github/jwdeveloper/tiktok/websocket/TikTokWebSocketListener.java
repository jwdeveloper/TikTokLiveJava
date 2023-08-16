package io.github.jwdeveloper.tiktok.websocket;


import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;
import io.github.jwdeveloper.tiktok.messages.WebcastWebsocketAck;
import io.github.jwdeveloper.tiktok.messages.WebcastWebsocketMessage;

import java.io.ByteArrayOutputStream;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.concurrent.CompletionStage;


public class TikTokWebSocketListener implements java.net.http.WebSocket.Listener {

    private ByteArrayOutputStream accumulatedData = new ByteArrayOutputStream();

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        try {
            var decoded = Base64.getEncoder().encodeToString(data.array());
            System.out.println(decoded);
            var bytes = new byte[data.remaining()];
            data.get(bytes);
            accumulatedData.write(bytes);
            if (last) {

                //handleBinary(webSocket, accumulatedData.toByteArray());
                accumulatedData.reset();
                accumulatedData = new ByteArrayOutputStream();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        webSocket.request(1);
        return null;
    }

    private void handleBinary(WebSocket webSocket, byte[] buffer) {
        try {

            var websocketMessage = WebcastWebsocketMessage.parseFrom(buffer);
            if (websocketMessage.getBinary().isEmpty()) {
                return;
            }
            sendAckId(webSocket, websocketMessage.getId());


            try {

                //error here
                var response = WebcastResponse.parseFrom(websocketMessage.getBinary());
                System.out.println("Works");
                //  handleResponse(response);
            } catch (Exception e) {
                throw new TikTokLiveException("Unabel to read WebcastResponse", e);
            }
        } catch (Exception e) {
            throw new TikTokLiveException("Unabel to read WebcastWebsocketMessage", e);
        }
    }


    private void sendAckId(WebSocket webSocket, long id) {
        var serverInfo = WebcastWebsocketAck
                .newBuilder()
                .setType("ack")
                .setId(id)
                .build();
        webSocket.sendBinary(serverInfo.toByteString().asReadOnlyByteBuffer(), true);
    }

    @Override
    public void onOpen(java.net.http.WebSocket webSocket) {
        System.out.println("WebSocket opened");
        webSocket.request(1);
    }

    @Override
    public void onError(java.net.http.WebSocket webSocket, Throwable error) {
        System.out.println("Error occurred: " + error.getMessage());
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onText(java.net.http.WebSocket webSocket, CharSequence data, boolean last) {
        System.out.println("Received onText: " + data);
        return java.net.http.WebSocket.Listener.super.onText(webSocket, data, last);
    }

    @Override
    public CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
        System.out.println("WebSocket closed with status code: " + statusCode + " and reason: " + reason);
        return java.net.http.WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }


}