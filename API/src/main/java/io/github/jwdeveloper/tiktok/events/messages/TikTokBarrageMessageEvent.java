package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.objects.BarrageData;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastBarrageMessage;
import lombok.Getter;

@Getter
public class TikTokBarrageMessageEvent extends TikTokEvent {
    private final Picture picture;

    private final Picture picture2;

    private final Picture picture3;

    private final User user;
    private final BarrageData barrageData;

    public TikTokBarrageMessageEvent(WebcastBarrageMessage msg) {
        super(msg.getHeader());

        picture = new Picture(msg.getPicture());
        picture2 = new Picture(msg.getPicture2());
        picture3 = new Picture(msg.getPicture3());
        user = new User(msg.getUserData().getUser());
        barrageData = new BarrageData(msg.getMessage().getEventType(),
                msg.getMessage().getLabel(),
                msg.getMessage().getData1List().stream().map(e ->
                {
                    var user = new User(e.getUser().getUser());
                    return new BarrageData.BarrageUser(user, e.getData2());
                }).toList()
        );
    }
}
