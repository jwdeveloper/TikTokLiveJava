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
package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastChatMessage;
import io.github.jwdeveloper.tiktok.messages.WebcastRoomPinMessage;
import lombok.Value;

import java.util.ArrayList;
import java.util.List;

/**
 * Triggered every time a new chat comment arrives.
 */
@Value
@EventMeta(eventType = EventType.Message)
public class TikTokCommentEvent extends TikTokHeaderEvent {
    User user;
    String text;
    String language;
    List<User> mentionedUsers;
    List<Picture> pictures;

    public TikTokCommentEvent(WebcastRoomPinMessage.RoomPinMessageData data) {
        super(data.getDetails().getRoomId(), data.getDetails().getMessageId(), data.getDetails().getServerTime());
        user = User.MapOrEmpty(data.getSender());
        text = data.getComment();
        language = data.getLanguage();
        mentionedUsers = new ArrayList<>();
        pictures = new ArrayList<>();
    }

    public TikTokCommentEvent(WebcastChatMessage msg) {
        super(msg.getCommon());
        user = User.MapOrEmpty(msg.getUser());
        text = msg.getContent();
        language = msg.getContentLanguage();
        mentionedUsers = List.of(User.MapOrEmpty(msg.getAtUser()));
        pictures = msg.getEmotesListList().stream().map(e ->Picture.Map(e.getEmote().getImage())).toList();
    }
}
