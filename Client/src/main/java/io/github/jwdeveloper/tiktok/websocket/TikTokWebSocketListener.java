/*
 * Copyright (c) 2023-2023 jwdeveloper jacekwoln@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package io.github.jwdeveloper.tiktok.websocket;

import com.google.protobuf.ByteString;
import io.github.jwdeveloper.tiktok.*;
import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.exceptions.TikTokProtocolBufferException;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.messages.webcast.*;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.nio.ByteBuffer;
import java.util.*;

public class TikTokWebSocketListener extends WebSocketClient {

    private final TikTokLiveMessageHandler messageHandler;
    private final TikTokLiveEventHandler tikTokEventHandler;
    private final LiveClient tikTokLiveClient;

    public TikTokWebSocketListener(URI serverUri,
                                   Map<String, String> httpHeaders,
                                   int connectTimeout,
                                   TikTokLiveMessageHandler messageHandler,
                                   TikTokLiveEventHandler tikTokEventHandler,
                                   LiveClient tikTokLiveClient) {
        super(serverUri, new Draft_6455(), httpHeaders, connectTimeout);
        this.messageHandler = messageHandler;
        this.tikTokEventHandler = tikTokEventHandler;
        this.tikTokLiveClient = tikTokLiveClient;
    }

    @Override
    public void onMessage(ByteBuffer bytes) {
        try {
            handleBinary(bytes.array());
        } catch (Exception e) {
            tikTokEventHandler.publish(tikTokLiveClient, new TikTokErrorEvent(e));
        }
        if (isNotClosing()) {
            sendPing();
        }
    }

    private void handleBinary(byte[] buffer) {
        var websocketPushFrameOptional = getWebcastPushFrame(buffer);
        if (websocketPushFrameOptional.isEmpty()) {
            return;
        }
        var websocketPushFrame = websocketPushFrameOptional.get();
        var webcastResponse = getWebResponseMessage(websocketPushFrame.getPayload());

        if (webcastResponse.getNeedsAck()) {
            var pushFrameBuilder = WebcastPushFrame.newBuilder();
            pushFrameBuilder.setPayloadType("ack");
            pushFrameBuilder.setLogId(websocketPushFrame.getLogId());
            pushFrameBuilder.setPayload(webcastResponse.getInternalExtBytes());
            if (isNotClosing())
            {
                this.send(pushFrameBuilder.build().toByteArray());
            }
        }
        messageHandler.handle(tikTokLiveClient, webcastResponse);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        tikTokEventHandler.publish(tikTokLiveClient, new TikTokConnectedEvent());
        if (isNotClosing()) {
            sendPing();
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        tikTokEventHandler.publish(tikTokLiveClient, new TikTokDisconnectedEvent(reason));
        tikTokLiveClient.disconnect();
    }

    @Override
    public void onError(Exception error) {
        tikTokEventHandler.publish(tikTokLiveClient, new TikTokErrorEvent(error));
        if (isNotClosing()) {
            sendPing();
        }
    }

    private Optional<WebcastPushFrame> getWebcastPushFrame(byte[] buffer) {
        try {
            var websocketMessage = WebcastPushFrame.parseFrom(buffer);
            if (websocketMessage.getPayload().isEmpty()) {
                return Optional.empty();
            }
            return Optional.of(websocketMessage);
        } catch (Exception e) {
            throw new TikTokProtocolBufferException("Unable to parse WebcastPushFrame", buffer, e);
        }
    }

    private WebcastResponse getWebResponseMessage(ByteString buffer) {
        try {
            return WebcastResponse.parseFrom(buffer);
        } catch (Exception e) {
            throw new TikTokProtocolBufferException("Unable to parse WebcastResponse", buffer.toByteArray(), e);
        }
    }

    private boolean isNotClosing() {
        return !isClosed() && !isClosing();
    }

    @Override
    public void onMessage(String s) {
        // System.err.println(s);
    }
}