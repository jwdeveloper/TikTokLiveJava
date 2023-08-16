package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastQuestionNewMessage;
import lombok.Getter;

@Getter
public class TikTokQuestionEvent extends TikTokEvent {
    private final Long questionId;

    private final String text;

    private final Long time;

    private User user;


    public TikTokQuestionEvent(WebcastQuestionNewMessage msg) {
        super(msg.getHeader());
        var data = msg.getDetails();
        questionId = data.getId();
        text = data.getText();
        time = data.getTimeStamp();
        if (data.hasUser()) {
            user = new User(data.getUser());
        }
    }
}
