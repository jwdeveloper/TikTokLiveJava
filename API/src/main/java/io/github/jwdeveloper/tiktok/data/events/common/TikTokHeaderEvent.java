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
package io.github.jwdeveloper.tiktok.data.events.common;

import io.github.jwdeveloper.tiktok.messages.data.Common;
import lombok.Getter;

@Getter
public class TikTokHeaderEvent extends TikTokEvent {
    private final long messageId;
    private final long roomId;
    private final long timeStamp;

    public TikTokHeaderEvent(Common header) {
        this(header.getMsgId(), header.getRoomId(), header.getCreateTime());
    }

    public TikTokHeaderEvent(long messageId, long roomId, long timeStamp) {
        this.messageId = messageId;
        this.roomId = roomId;
        this.timeStamp = timeStamp;
    }

    public TikTokHeaderEvent() {
        messageId = 0;
        roomId = 0;
        timeStamp = 0;
    }
}
