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
package io.github.jwdeveloper.tiktok.data.events.link;

import io.github.jwdeveloper.tiktok.annotations.*;
import io.github.jwdeveloper.tiktok.data.models.users.*;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMessage;
import lombok.Getter;

import java.util.List;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokLinkListChangeEvent extends TikTokLinkEvent {

    private final List<ListUser> linkedUsers, appliedUsers, connectingUsers;

    public TikTokLinkListChangeEvent(WebcastLinkMessage msg) {
        super(msg);
        if (!msg.hasListChangeContent())
            throw new IllegalArgumentException("Expected WebcastLinkMessage with List Change Content!");

        var content = msg.getListChangeContent();
        this.linkedUsers = content.getLinkedUsersList().stream().map(ListUser::map).toList();
        this.appliedUsers = content.getAppliedUsersList().stream().map(ListUser::map).toList();
        this.connectingUsers = content.getConnectingUsersList().stream().map(ListUser::map).toList();
    }
}