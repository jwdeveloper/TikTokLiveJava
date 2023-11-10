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
package io.github.jwdeveloper.tiktok.data.events.envelop;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.data.models.Text;

import io.github.jwdeveloper.tiktok.data.models.chest.Chest;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastEnvelopeMessage;
import lombok.Value;

import java.util.Date;


@EventMeta(eventType = EventType.Message)
@Value
public class TikTokChestEvent extends TikTokHeaderEvent {

    /**
     *  Chest target
     */
    Chest chest;

    /**
     * User that send a chest
     */
    User user;

    /**
     * Time when chest has been open
     */
    Date openedAt;

    public TikTokChestEvent(Chest chest, WebcastEnvelopeMessage msg) {
        super(msg.getCommon());
        this.chest = chest;

        var text = Text.map(msg.getCommon().getDisplayText());
        var userPiece = (Text.UserTextPiece) text.getTextPieces().get(0);
        user = userPiece.getUser();


        var envelopInfo = msg.getEnvelopeInfo();

        openedAt = new Date(envelopInfo.getUnpackAt());

    }
}
