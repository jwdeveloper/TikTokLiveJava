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
package io.github.jwdeveloper.tiktok.mockClient.mocks;

import io.github.jwdeveloper.tiktok.TikTokLiveClientBuilder;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveMessageException;
import io.github.jwdeveloper.tiktok.handlers.TikTokMessageHandler;
import io.github.jwdeveloper.tiktok.live.LiveClient;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.websocket.SocketClient;

import java.util.Base64;
import java.util.Stack;
import java.util.logging.Logger;


public class WebsocketClientMock implements SocketClient {
    Logger logger;
    Stack<WebcastResponse> responses;
    TikTokMessageHandler messageHandler;

    private boolean isRunning;

    public WebsocketClientMock(Logger logger, Stack<WebcastResponse> responses, TikTokMessageHandler messageHandler) {
        this.logger = logger;
        this.responses = responses;
        this.messageHandler = messageHandler;
    }


    public WebsocketClientMock addResponse(String value) {
        var bytes = Base64.getDecoder().decode(value);
        return addResponse(bytes);
    }

    public WebsocketClientMock addResponse(byte[] bytes) {
        try {
            var response = WebcastResponse.parseFrom(bytes);
            return addResponse(response);
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse response from bytes", e);
        }
    }

    public WebsocketClientMock addResponse(WebcastResponse message) {
        responses.push(message);
        return this;
    }


    @Override
    public void start(WebcastResponse webcastResponse, LiveClient tikTokLiveClient) {
        logger.info("Running message: " + responses.size());
        isRunning =true;
        while (isRunning) {
            do {
                if(responses.isEmpty())
                {
                    break;
                }
                var response = responses.pop();
                for (var message : response.getMessagesList()) {
                    try {
                        messageHandler.handleSingleMessage(tikTokLiveClient, message);
                    } catch (Exception e) {
                        logger.info("Unable to parse message for response " + response.getCursor());
                        throw new TikTokLiveMessageException(message, response, e);
                    }
                }
            }
            while (!responses.isEmpty());
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

    @Override
    public void stop() {
        isRunning = true;
    }
}
