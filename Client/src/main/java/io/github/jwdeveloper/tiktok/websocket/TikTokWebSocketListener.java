package io.github.jwdeveloper.tiktok.websocket;


import io.github.jwdeveloper.tiktok.events.messages.TikTokConnectedEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokDisconnectedEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageParsingException;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.handlers.WebResponseHandler;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;
import io.github.jwdeveloper.tiktok.messages.WebcastWebsocketAck;
import io.github.jwdeveloper.tiktok.messages.WebcastWebsocketMessage;

import java.io.ByteArrayOutputStream;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;


public class TikTokWebSocketListener implements java.net.http.WebSocket.Listener {

    private final ByteArrayOutputStream accumulatedData = new ByteArrayOutputStream();
    private final WebResponseHandler webResponseHandler;
    private final TikTokEventHandler tikTokEventHandler;

    public TikTokWebSocketListener(WebResponseHandler webResponseHandler, TikTokEventHandler tikTokEventHandler) {
        this.webResponseHandler = webResponseHandler;
        this.tikTokEventHandler = tikTokEventHandler;
    }

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
        try {
            var bytes = new byte[data.remaining()];
            data.get(bytes);
            accumulatedData.write(bytes);
            if (last) {
                handleBinary(webSocket, accumulatedData.toByteArray());
                accumulatedData.reset();
            }
        } catch (Exception e) {
            tikTokEventHandler.publish(new TikTokErrorEvent(e));
        }
        webSocket.request(1);
        return null;
    }

    @Override
    public void onOpen(java.net.http.WebSocket webSocket) {
        tikTokEventHandler.publish(new TikTokConnectedEvent());
        webSocket.request(1);
    }

    @Override
    public void onError(java.net.http.WebSocket webSocket, Throwable error) {
        tikTokEventHandler.publish(new TikTokErrorEvent(error));
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
        tikTokEventHandler.publish(new TikTokDisconnectedEvent());
        return java.net.http.WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    private void handleBinary(WebSocket webSocket, byte[] buffer) {
        try {

            var websocketMessage = WebcastWebsocketMessage.parseFrom(buffer);
            if (websocketMessage.getBinary().isEmpty()) {
                return;
            }
            sendAckId(webSocket, websocketMessage.getId());
            try {
                var response = WebcastResponse.parseFrom(websocketMessage.getBinary());
                webResponseHandler.handle(response);
            } catch (Exception e) {
                throw new TikTokLiveMessageParsingException("Unable to read WebcastResponse", e);
            }
        } catch (Exception e) {
            throw new TikTokLiveMessageParsingException("Unable to read WebcastWebsocketMessage", e);
        }
    }

    private void pingTask(WebSocket webSocket) throws InterruptedException {
        while (true) {
            byte[] message = new byte[]{58, 2, 104, 98};
            ByteBuffer buffer = ByteBuffer.wrap(message);
            while (buffer.hasRemaining()) {
                webSocket.sendPing(buffer);
            }
            buffer.clear();
            Thread.sleep(10);
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

}