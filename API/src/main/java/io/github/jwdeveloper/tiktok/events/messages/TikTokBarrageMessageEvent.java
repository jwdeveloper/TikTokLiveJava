package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.BarrageData;
import io.github.jwdeveloper.tiktok.events.objects.Picture;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastBarrageMessage;
import lombok.Value;

@Value
@EventMeta(eventType = EventType.Message)
public class TikTokBarrageMessageEvent extends TikTokHeaderEvent {
    Picture picture;
    Picture picture2;
    Picture picture3;
    User user;
    BarrageData barrageData;
    public TikTokBarrageMessageEvent(WebcastBarrageMessage msg) {
        super(msg.getHeader());

        picture = new Picture(msg.getImage());
        picture2 = new Picture(msg.getImage2());
        picture3 = new Picture(msg.getImage3());
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
