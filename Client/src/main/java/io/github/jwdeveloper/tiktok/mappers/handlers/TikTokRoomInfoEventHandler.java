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
package io.github.jwdeveloper.tiktok.mappers.handlers;

import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.data.events.TikTokSubscribeEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokUnhandledMemberEvent;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomInfoEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.models.RankingUser;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.mappers.LiveMapperHelper;
import io.github.jwdeveloper.tiktok.mappers.data.MappingResult;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLikeMessage;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLiveIntroMessage;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastMemberMessage;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastRoomUserSeqMessage;
import lombok.SneakyThrows;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TikTokRoomInfoEventHandler {
    private final TikTokRoomInfo roomInfo;

    public TikTokRoomInfoEventHandler(TikTokRoomInfo roomInfo) {
        this.roomInfo = roomInfo;
    }

    public TikTokEvent handleRoomInfo(Consumer<TikTokRoomInfo> consumer) {
        consumer.accept(roomInfo);
        return new TikTokRoomInfoEvent(roomInfo);
    }

    @SneakyThrows
    public TikTokEvent handleUserRanking(byte[] msg) {
        var message = WebcastRoomUserSeqMessage.parseFrom(msg);
        var totalUsers = message.getTotalUser();
        var userRanking = message.getRanksListList().stream().map(RankingUser::new)
                .sorted((ru1, ru2) -> Integer.compare(ru2.getScore(), ru1.getScore()))
                .collect(Collectors.toList());

        return handleRoomInfo(tikTokRoomInfo ->
        {
            tikTokRoomInfo.setTotalViewersCount(totalUsers);
            tikTokRoomInfo.updateRanking(userRanking);
        });
    }

    @SneakyThrows
    public TikTokEvent handleIntro(byte[] msg) {
        var message = WebcastLiveIntroMessage.parseFrom(msg);
        var hostUser = User.map(message.getHost());
        var language = message.getLanguage();

        return handleRoomInfo(tikTokRoomInfo ->
        {
            if (tikTokRoomInfo.getHost() == null) {
                tikTokRoomInfo.setHost(hostUser);
            }
            tikTokRoomInfo.setLanguage(language);
        });
    }

    @SneakyThrows
    public MappingResult handleMemberMessage(byte[] msg, String name, LiveMapperHelper helper) {
        var message = WebcastMemberMessage.parseFrom(msg);

        var event = switch (message.getAction()) {
            case JOINED -> new TikTokJoinEvent(message);
            case SUBSCRIBED -> new TikTokSubscribeEvent(message);
            default -> new TikTokUnhandledMemberEvent(message);
        };

        var roomInfoEvent = this.handleRoomInfo(tikTokRoomInfo ->
        {
            tikTokRoomInfo.setViewersCount(message.getMemberCount());
        });
        return MappingResult.of(message, List.of(event, roomInfoEvent));
    }

    @SneakyThrows
    public MappingResult handleLike(byte[] msg, String name, LiveMapperHelper helper) {
        var message = WebcastLikeMessage.parseFrom(msg);
        var event = new TikTokLikeEvent(message);
        var roomInfoEvent = this.handleRoomInfo(tikTokRoomInfo ->
        {
            tikTokRoomInfo.setLikesCount(event.getTotalLikes());
        });
        return MappingResult.of(message, List.of(event, roomInfoEvent));
    }
}
