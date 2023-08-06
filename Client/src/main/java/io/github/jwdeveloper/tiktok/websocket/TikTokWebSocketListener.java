package io.github.jwdeveloper.tiktok.websocket;

import io.github.jwdeveloper.generated.WebcastMessageEvent;
import io.github.jwdeveloper.generated.WebcastResponse;
import io.github.jwdeveloper.generated.WebcastWebsocketAck;
import io.github.jwdeveloper.generated.WebcastWebsocketMessage;
import io.github.jwdeveloper.tiktok.TikTokLiveException;

import java.net.http.WebSocket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;


public class TikTokWebSocketListener implements java.net.http.WebSocket.Listener {

    List<ByteBuffer> parts = new ArrayList<>();
    CompletableFuture<?> accumulatedMessage = new CompletableFuture<>();

    @Override
    public CompletionStage<?> onBinary(WebSocket webSocket, ByteBuffer data, boolean last) {
       System.out.println("Received onBinary: " + data + " " + last);
        parts.add(data);
        webSocket.request(1);
        if (last) {
            var buffer = combineBuffer(parts);
            accumulatedMessage.complete(null);
            CompletionStage<?> cf = accumulatedMessage;
            accumulatedMessage = new CompletableFuture<>();
            handleBinary(webSocket, buffer);
            parts = new ArrayList<>();
            return cf;
        }
        return accumulatedMessage;
    }


    public ByteBuffer combineBuffer(List<ByteBuffer> buffers) {
        int totalCapacity = buffers.stream().mapToInt(ByteBuffer::remaining).sum();
        ByteBuffer combined = ByteBuffer.allocate(totalCapacity);
        for (ByteBuffer buffer : buffers) {
            combined.put(buffer);
        }
        combined.flip();
        return combined;
    }


    private void handleBinary(WebSocket webSocket, ByteBuffer buffer) {
        try {
            WebcastWebsocketMessage websocketMessage = WebcastWebsocketMessage.parseFrom(buffer);

            if(websocketMessage.getBinary().isEmpty())
            {
                return;
            }
           // System.out.println(websocketMessage.getBinary());
            try {
                var response = WebcastResponse.parseFrom(websocketMessage.getBinary());
                var serverInfo = WebcastWebsocketAck
                        .newBuilder()
                        .setType("ack")
                        .setId(websocketMessage.getId())
                        .build();
                webSocket.sendBinary(serverInfo.toByteString().asReadOnlyByteBuffer(), true);

                System.out.println("Works");
              //  handleResponse(response);
            } catch (Exception e) {
               // throw new TikTokLiveException("Unabel to read WebcastResponse");
              System.out.println("Unable to read WebcastResponse");
            }
        } catch (Exception e) {

            System.out.println("Unable to read WebcastWebsocketMessage");
            //throw new TikTokLiveException("Unabel to read WebcastWebsocketMessage");
        }
    }


    public void handleResponse(WebcastResponse webcastResponse) {
        System.out.println("Handling response: Messages"+webcastResponse.getMessagesList().size());

        for(var message  : webcastResponse.getMessagesList())
        {


        }
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