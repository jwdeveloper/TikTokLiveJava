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
package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.data.events.TikTokErrorEvent;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.models.gifts.Gift;
import io.github.jwdeveloper.tiktok.exceptions.TikTokMessageMappingException;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastChatMessage;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastGiftMessage;
import io.github.jwdeveloper.tiktok.utils.ProtocolUtils;

public class CustomMappingExample {

    public static void main(String[] args) {
        TikTokLive.newClient("vadimpyrography")
                .onCustomEvent(CustomChatEvent.class, (liveClient, event) ->
                {
                    System.out.println("hello world!");
                })
                .onError((liveClient, event) ->
                {
                    event.getException().printStackTrace();
                })
                .onMapping(mapper ->
                {
                    mapper.webcastObjectToEvent(WebcastChatMessage.class, chatMessage ->
                    {
                        var language = chatMessage.getContentLanguage();
                        var userName = chatMessage.getUser().getNickname();
                        var message = chatMessage.getContent();
                        return new CustomChatEvent(language, userName, message);
                    });
                    mapper.bytesToEvent("WebcastGiftMessage", bytes ->
                    {
                        try
                        {
                            var event = WebcastGiftMessage.parseFrom(bytes);
                            return new TikTokGiftEvent(Gift.ROSA, event);
                        } catch (Exception e) {
                            throw new TikTokMessageMappingException("unable to map gift message!", e);
                        }
                    });

                    mapper.bytesToEvent("WebcastMemberMessage",bytes ->
                    {
                        //displaying unknown messages from tiktok
                        var structure = ProtocolUtils.getProtocolBufferStructure(bytes);
                        System.out.println(structure.toJson());
                        return new TikTokErrorEvent(new RuntimeException("Message not implemented"));
                    });
                }).buildAndConnect();

    }

    public static class CustomChatEvent extends TikTokEvent {
        private final String langauge;
        private final String userName;
        private final String message;

        public CustomChatEvent(String language, String userName, String message) {
            this.langauge = language;
            this.userName = userName;
            this.message = message;
        }

        public String getLangauge() {
            return langauge;
        }

        public String getUserName() {
            return userName;
        }

        public String getMessage() {
            return message;
        }

        @Override
        public String toString() {
            return "CustomChatEvent{" +
                    "language='" + langauge + '\'' +
                    ", userName='" + userName + '\'' +
                    ", message='" + message + '\'' +
                    '}';
        }

    }
}
