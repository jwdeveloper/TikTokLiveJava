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
package io.github.jwdeveloper.tiktok.data.events.websocket;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.data.dto.MessageMetaData;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastGiftMessage;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Value;

import java.time.Duration;



@Getter
@AllArgsConstructor
@EventMeta(eventType = EventType.Debug)
public class TikTokWebsocketMessageEvent extends TikTokEvent {

    /*
     * Original message that is coming from TikTok
     *  message.method - Name of message type, for example "WebcastGiftMessage"
     *  message.payload - Bytes array that contains actual data of message.
     *                    Example of parsing, WebcastGiftMessage giftMessage = WebcastGiftMessage.parseFrom(message.getPayload());
     */
    private WebcastResponse.Message message;

    /*
     * TikTokLiveJava event that was created from TikTok message data
     * Example: TikTokGiftEvent
     */
    private TikTokEvent event;

    /*
     * Metadata information about mapping message to event, such as time and stuff
     */
    private MessageMetaData metaData;


}
