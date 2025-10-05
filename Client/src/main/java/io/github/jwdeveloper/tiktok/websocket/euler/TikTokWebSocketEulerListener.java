/*
 * Copyright (c) 2023-2024 jwdeveloper jacekwoln@gmail.com
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
package io.github.jwdeveloper.tiktok.websocket.euler;

import com.google.gson.*;
import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomInfoEvent;
import io.github.jwdeveloper.tiktok.data.requests.LiveUserData;
import io.github.jwdeveloper.tiktok.http.mappers.LiveUserDataMapper;
import io.github.jwdeveloper.tiktok.live.*;
import io.github.jwdeveloper.tiktok.websocket.TikTokWebSocketListener;

import java.net.URI;
import java.util.Map;

public class TikTokWebSocketEulerListener extends TikTokWebSocketListener
{

    public TikTokWebSocketEulerListener(URI serverUri,
                                        Map<String, String> httpHeaders,
                                        int connectTimeout,
                                        LiveMessagesHandler messageHandler,
                                        LiveEventsHandler tikTokEventHandler,
                                        LiveClient tikTokLiveClient) {
        super(serverUri, httpHeaders, connectTimeout, messageHandler, tikTokEventHandler, tikTokLiveClient);
    }

	@Override
    public void onMessage(String raw) {
        try {
            JsonElement element = JsonParser.parseString(raw);
            if (element instanceof JsonObject o) {
                if (o.get("messages") instanceof JsonArray msgs) {
                    for (JsonElement msg : msgs) {
                        if (msg instanceof JsonObject oMsg) {
                            switch (oMsg.get("type").getAsString()) { // Should only receive these 2 types ever
                                case "workerInfo" -> liveClient.getLogger().info(oMsg.toString()); // Always 1st message
                                case "roomInfo" -> { // Always 2nd message
									LiveUserData.Response data = LiveUserDataMapper.map(oMsg.getAsJsonObject("data").getAsJsonObject("raw").toString(), liveClient.getLogger());
                                    liveClient.getRoomInfo().copy(data.getRoomInfo());
                                    eventHandler.publish(liveClient, new TikTokRoomInfoEvent(liveClient.getRoomInfo()));
                                }
                            }
                        }
                    }
                }
            } else
                throw new IllegalArgumentException("Invalid JsonObject: "+element);
        } catch (Exception e) {
            e.printStackTrace();
            eventHandler.publish(liveClient, new TikTokErrorEvent(e));
        }
        if (isOpen()) {
            sendPing();
        }
    }
}