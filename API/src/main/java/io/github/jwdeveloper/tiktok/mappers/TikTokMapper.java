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
package io.github.jwdeveloper.tiktok.mappers;

import com.google.protobuf.GeneratedMessageV3;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;

import java.util.List;
import java.util.function.Function;

public interface TikTokMapper {


    /**
     * Triggered when `sourceClass` is mapped,
     * input is bytes that are coming from TikTok in `sourceClass` packet
     * output is TikTok event we want to create
     * <p>
     * bytesToEvent(WebcastGiftMessage.class, bytes ->
     * {
     * var giftMessage = WebcastGiftMessage.parseFrom(bytes);
     * var giftName = giftMessage.getGift().getName();
     * return new TikTokEvent(Gift.ROSE, giftMessage);
     * })
     *
     * @param sourceClass protocol buffer webcast class
     * @param onMapping   lambda function that is triggered on mapping. takes as input ProtocolBuffer object and as output TikTokEvent
     */
    void bytesToEvent(Class<? extends GeneratedMessageV3> sourceClass, Function<byte[], TikTokEvent> onMapping);

    void bytesToEvents(Class<? extends GeneratedMessageV3> sourceClass, Function<byte[], List<TikTokEvent>> onMapping);


    /**
     * In case you found some TikTok message that has not Webcast class use this method
     *
     * @param messageName Name of TikTok data event
     * @param onMapping   lambda function that is triggered on mapping. takes as input ProtocolBuffer object and as output TikTokEvent
     */
    void bytesToEvent(String messageName, Function<byte[], TikTokEvent> onMapping);

    void bytesToEvents(String messageName, Function<byte[], List<TikTokEvent>> onMapping);


    /**
     * This method can be used to override default mapping for
     * certain TikTok incoming data message. For this example
     * we are overriding WebcastGiftMessage and retuning CustomGiftEvent
     * instead of TikTokGiftEvent
     * <p>
     * webcastObjectToEvent(WebcastGiftMessage.class, webcastGiftMessage ->
     * {
     * var giftName = webcastGiftMessage.getGift().getName();
     * var user = webcastGiftMessage.getUser().getNickname();
     * return new CustomGiftEvent(giftName, user);
     * })
     *
     * @param sourceClass ProtocolBuffer class that represent incoming custom data, hint class should starts with Webcast prefix
     * @param onMapping   lambda function that is triggered on mapping. takes as input ProtocolBuffer object and as output TikTokEvent
     */
    <T extends GeneratedMessageV3> void webcastObjectToEvent(Class<T> sourceClass, Function<T, TikTokEvent> onMapping);

    <T extends GeneratedMessageV3> void webcastObjectToEvents(Class<T> sourceClass, Function<T, List<TikTokEvent>> onMapping);

    /**
     * Triggered when `sourceClass` is mapped,
     * looking for constructor in `outputClass` with one parameter that is of type `sourceClass`
     * and created instance of object from this constructor
     *
     * @param sourceClass protocol buffer webcast class
     * @param outputClass TikTok event class
     */
    void webcastObjectToConstructor(Class<? extends GeneratedMessageV3> sourceClass, Class<? extends TikTokEvent> outputClass);

}
