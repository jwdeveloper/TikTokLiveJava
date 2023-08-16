package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastUnauthorizedMemberMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
public class TikTokUnauthorizedMemberEvent extends TikTokEvent {
    private final String data;

    private final UnauthorizedMemberData event;

    private final UnauthorizedMemberData underlying;

    public TikTokUnauthorizedMemberEvent(WebcastUnauthorizedMemberMessage msg) {
        super(msg.getHeader());

        data = msg.getData2();
        event = new UnauthorizedMemberData(msg.getDetails1().getType(), msg.getDetails1().getLabel());
        underlying = new UnauthorizedMemberData(msg.getDetails2().getType(), msg.getDetails2().getLabel());
    }


    @Getter
    @AllArgsConstructor
    public class UnauthorizedMemberData {
        private final String data1;

        private final String data2;
    }

}
