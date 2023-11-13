package io.github.jwdeveloper.tiktok.handlers.events;

import io.github.jwdeveloper.tiktok.TikTokRoomInfo;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomInfoEvent;
import io.github.jwdeveloper.tiktok.data.models.RankingUser;
import io.github.jwdeveloper.tiktok.data.models.users.User;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLiveIntroMessage;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastRoomUserSeqMessage;
import lombok.SneakyThrows;

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
            if(tikTokRoomInfo.getHost() == null)
            {
                tikTokRoomInfo.setHost(hostUser);
            }
            tikTokRoomInfo.setLanguage(language);
        });
    }
}
