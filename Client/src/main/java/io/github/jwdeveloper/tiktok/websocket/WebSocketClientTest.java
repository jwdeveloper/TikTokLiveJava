package io.github.jwdeveloper.tiktok.websocket;

import com.google.protobuf.ByteString;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveException;
import io.github.jwdeveloper.tiktok.handlers.WebResponseHandlerBase;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;
import io.github.jwdeveloper.tiktok.messages.WebcastWebsocketAck;
import io.github.jwdeveloper.tiktok.messages.WebcastWebsocketMessage;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class WebSocketClientTest extends WebSocketClient {

    private boolean debbug = false;

    private final WebResponseHandlerBase webResponseHandler;


    public WebSocketClientTest(URI serverUri,
                               Draft protocolDraft,
                               Map<String, String> httpHeaders,
                               int connectTimeout,
                               WebResponseHandlerBase webResponseHandler) {
        super(serverUri, protocolDraft, httpHeaders, connectTimeout);
        this.webResponseHandler = webResponseHandler;
    }

    @Override
    public void onMessage(ByteBuffer data) {
        sendPing();
        //System.out.println("onMessage Binary");
        var bytes = new byte[data.remaining()];
        data.get(bytes);
        if(debbug)
        {
            var decoded = Base64.getEncoder().encodeToString(bytes);
            // System.out.println(decoded);
        }
        handleBinary(bytes);
    }

    private void handleBinary(byte[] buffer) {
        try {

            var websocketMessage = WebcastWebsocketMessage.parseFrom(buffer);
            if (websocketMessage.getBinary().isEmpty()) {
                return;
            }
            try {
                var response = WebcastResponse.parseFrom(websocketMessage.getBinary());
                sendAckId(websocketMessage.getId());
                webResponseHandler.handle(response);
            } catch (Exception e) {
               throw new TikTokLiveException("Unabel to read WebcastResponse", e);
            }
        } catch (Exception e) {
           throw new TikTokLiveException("Unabel to read WebcastWebsocketMessage", e);
        }
    }


    public byte[] unGunzipFile(ByteString byteString) {

        try {

            GZIPInputStream gZIPInputStream = new GZIPInputStream(byteString.newInput());


            var bytes = gZIPInputStream.readAllBytes();

            gZIPInputStream.close();


            return bytes;

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new byte[0];
    }
    private void sendAckId(long id) {
        var serverInfo = WebcastWebsocketAck
                .newBuilder()
                .setType("ack")
                .setId(id)
                .build();
        send(serverInfo.toByteString().asReadOnlyByteBuffer());
    }



    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        System.out.println("onOpen");
        sendPing();
    }

    @Override
    public void onMessage(String s) {
        sendPing();
    }

    @Override
    public void onClose(int i, String s, boolean b) {
        System.out.println("onClose");
    }

    @Override
    public void onError(Exception e) {
        System.out.println("error");
        e.printStackTrace();
    }
}
