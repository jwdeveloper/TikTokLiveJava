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

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.mappers.data.MappingResult;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastChatMessage;

public class CustomMappingExample {

    public static void main(String[] args) {
        TikTokLive.newClient("saszareznikow")
                .mappings(mapper ->
                {
                    mapper.forMessage(WebcastChatMessage.class)
                            .onBeforeMapping((inputBytes, messageName, mapperHelper) ->
                            {
                                System.out.println("===============================");
                                System.out.println("OnBefore mapping: " + messageName);
                                return inputBytes;
                            })
                            .onMapping((inputBytes, messageName, mapperHelper) ->
                            {
                                System.out.println("onMapping mapping: " + messageName);
                                var message = mapperHelper.bytesToWebcastObject(inputBytes, WebcastChatMessage.class);
                                var language = message.getContentLanguage();
                                var userName = message.getUser().getNickname();
                                var content = message.getContent();
                                var event = new CustomChatEvent(language, userName, content);
                                return MappingResult.of(message, event);
                            })
                            .onAfterMapping(mappingResult ->
                            {
                                var source = mappingResult.getSource();
                                var events = mappingResult.getEvents();
                                System.out.println("onAfter mapping, " + source.getClass().getSimpleName() + " was mapped to " + events.size() + " events");
                                return events;
                            });
                    /*
                      There might be cast that we don't have Webcast class for incoming message from TikTok
                      `mapperHelper.bytesToProtoBufferStructure` but you can still investigate message structure
                       by using helper methods
                     */
                    mapper.forMessage("WebcastMemberMessage")
                            .onBeforeMapping((inputBytes, messageName, mapperHelper) ->
                            {
                                if (mapperHelper.isMessageHasProtoClass(messageName)) {
                                    var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, messageName);
                                    //    System.out.println(mapperHelper.toJson(messageObject));
                                } else {
                                    var structure = mapperHelper.bytesToProtoBufferStructure(inputBytes);
                                    //     System.out.println(structure.toJson());
                                }
                                return inputBytes;
                            });
                })
                .onError((liveClient, event) ->
                {
                    event.getException().printStackTrace();
                })
                .buildAndConnect();

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
