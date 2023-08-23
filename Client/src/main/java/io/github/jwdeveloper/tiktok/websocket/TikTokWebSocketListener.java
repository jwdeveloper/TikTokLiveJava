package io.github.jwdeveloper.tiktok.websocket;


import com.google.protobuf.ByteString;
import io.github.jwdeveloper.tiktok.TikTokLiveClient;
import io.github.jwdeveloper.tiktok.events.messages.TikTokConnectedEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokDisconnectedEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.exceptions.TikTokProtocolBufferException;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventHandler;
import io.github.jwdeveloper.tiktok.handlers.TikTokMessageHandlerRegistration;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;
import io.github.jwdeveloper.tiktok.messages.WebcastWebsocketAck;
import io.github.jwdeveloper.tiktok.messages.WebcastWebsocketMessage;

import java.io.ByteArrayOutputStream;
import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.Optional;
import java.util.concurrent.CompletionStage;


public class TikTokWebSocketListener implements java.net.http.WebSocket.Listener {

    private final ByteArrayOutputStream accumulatedData = new ByteArrayOutputStream();
    private final TikTokMessageHandlerRegistration webResponseHandler;
    private final TikTokEventHandler tikTokEventHandler;
    private final TikTokLiveClient tikTokLiveClient;

    public TikTokWebSocketListener(TikTokMessageHandlerRegistration webResponseHandler,
                                   TikTokEventHandler tikTokEventHandler,
                                   TikTokLiveClient tikTokLiveClient) {
        this.webResponseHandler = webResponseHandler;
        this.tikTokEventHandler = tikTokEventHandler;
        this.tikTokLiveClient = tikTokLiveClient;
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
            tikTokEventHandler.publish(tikTokLiveClient, new TikTokErrorEvent(e));
        }
        webSocket.request(1);
        return null;
    }

    @Override
    public void onOpen(java.net.http.WebSocket webSocket) {
        tikTokEventHandler.publish(tikTokLiveClient,new TikTokConnectedEvent());
        webSocket.request(1);
    }

    @Override
    public void onError(java.net.http.WebSocket webSocket, Throwable error) {
        tikTokEventHandler.publish(tikTokLiveClient,new TikTokErrorEvent(error));
        webSocket.request(1);
    }

    @Override
    public CompletionStage<?> onClose(java.net.http.WebSocket webSocket, int statusCode, String reason) {
        tikTokEventHandler.publish(tikTokLiveClient,new TikTokDisconnectedEvent());
        return java.net.http.WebSocket.Listener.super.onClose(webSocket, statusCode, reason);
    }

    private void handleBinary(WebSocket webSocket, byte[] buffer) {
        var websocketMessageOptional = getWebcastWebsocketMessage(buffer);
        if (websocketMessageOptional.isEmpty()) {
            return;
        }
        var websocketMessage = websocketMessageOptional.get();
        sendAckId(webSocket, websocketMessage.getId());

        var webResponse = getWebResponseMessage(websocketMessage.getBinary());
        webResponseHandler.handle(tikTokLiveClient, webResponse);
    }

    private Optional<WebcastWebsocketMessage> getWebcastWebsocketMessage(byte[] buffer) {
        try {
            var websocketMessage = WebcastWebsocketMessage.parseFrom(buffer);
            if (websocketMessage.getBinary().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(websocketMessage);
        } catch (Exception e) {
            throw new TikTokProtocolBufferException("Unable to parse WebcastWebsocketMessage", buffer, e);
        }
    }

    private WebcastResponse getWebResponseMessage(ByteString buffer) {
        try {
            return WebcastResponse.parseFrom(buffer);
        } catch (Exception e) {
            throw new TikTokProtocolBufferException("Unable to parse WebcastResponse", buffer.toByteArray(), e);
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