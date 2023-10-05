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
package io.github.jwdeveloper.tiktok.tools.tester;

import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.events.messages.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokGiftComboFinishedEvent;
import io.github.jwdeveloper.tiktok.events.messages.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.gifts.TikTokGiftManager;
import io.github.jwdeveloper.tiktok.handlers.TikTokEventObserver;
import io.github.jwdeveloper.tiktok.handlers.TikTokMessageHandlerRegistration;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.tools.collector.db.TikTokDatabase;

import java.util.Base64;

public class RunTester {


    public static void main(String[] args) throws Exception {
        var db = new TikTokDatabase("test");
        db.init();
        var errors = db.selectErrors();




        var handler = getMessageHandler();
        for (var error : errors) {

            var bytes = Base64.getDecoder().decode(error.getResponse());
            var response = WebcastResponse.parseFrom(bytes);
            handler.handle(null, response);
        }

        var messags = db.selectMessages();
        for (var msg : messags) {
            if (!msg.getEventName().contains("Gift")) {
                continue;
            }
            var bytes = Base64.getDecoder().decode(msg.getEventContent());
            var response = WebcastResponse.Message.parseFrom(bytes);
            handler.handleSingleMessage(null, response);
        }


    }

    public static TikTokMessageHandlerRegistration getMessageHandler() {
        var observer = new TikTokEventObserver();
        observer.<TikTokGiftEvent>subscribe(TikTokGiftEvent.class, (liveClient, event) ->
        {
            var sb = new StringBuilder();
            sb.append("Event: " + event.getGift());
            sb.append(" combo: " + event.getComboCount());
            sb.append(" index " + event.getComboIndex());
            sb.append(" sender " + event.getSender().getNickName());
            System.out.println(sb.toString());
        });
        observer.<TikTokGiftComboFinishedEvent>subscribe(TikTokGiftComboFinishedEvent.class, (liveClient, event) ->
        {
            System.out.println("Combo finished event! " + event.getComboCount() + " " + event.getGift());
        });
        observer.<TikTokErrorEvent>subscribe(TikTokErrorEvent.class, (liveClient, event) ->
        {
            event.getException().printStackTrace();
        });
        var roomInfo = new TikTokRoomInfo();
        var manager = new TikTokGiftManager();
        return new TikTokMessageHandlerRegistration(observer, manager, roomInfo);
    }
}
