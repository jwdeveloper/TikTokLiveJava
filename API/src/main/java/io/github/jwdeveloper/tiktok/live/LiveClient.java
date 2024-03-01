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
package io.github.jwdeveloper.tiktok.live;

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.listener.ListenersManager;
import io.github.jwdeveloper.tiktok.listener.TikTokEventListener;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.logging.Logger;

public interface LiveClient {

    /**
     * Connects to the live stream.
     */
    void connect();


    /**
     * Connects in asynchronous way
     * When connected Consumer returns instance of LiveClient
     */
    void connectAsync(Consumer<LiveClient> onConnection);

    /**
     * Connects in asynchronous way
     */
    CompletableFuture<LiveClient> connectAsync();


    /**
     * Disconnects the connection.
     */
    void disconnect();


    /**
     * Use to manually invoke event
     */
    void publishEvent(TikTokEvent event);


    /**
     * @param webcastMessageName name of TikTok protocol-buffer message
     * @param payloadBase64      protocol-buffer message bytes payload
     */
    void publishMessage(String webcastMessageName, String payloadBase64);

    void publishMessage(String webcastMessageName, byte[] payload);

    /**
     * Get information about gifts
     */
    GiftsManager getGiftManager();

    /**
     * Gets the current room info from TikTok API including streamer info, room status and statistics.
     */
    LiveRoomInfo getRoomInfo();

    /**
     * Manage TikTokEventListener
     *
     * @see TikTokEventListener
     */
    ListenersManager getListenersManager();

    /**
     * Logger
     */
    Logger getLogger();
}
