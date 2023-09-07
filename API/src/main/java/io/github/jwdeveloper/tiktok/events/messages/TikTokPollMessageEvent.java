package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.events.objects.PollOption;
import io.github.jwdeveloper.tiktok.events.objects.User;
import io.github.jwdeveloper.tiktok.messages.WebcastPollMessage;
import lombok.Getter;

import java.util.List;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokPollMessageEvent extends TikTokHeaderEvent {
    private final Long id;

    private final PollOption option1;

    private final PollOption option2;

    private final List<PollOption.Option> options;

    public TikTokPollMessageEvent(WebcastPollMessage msg) {
        super(msg.getHeader());
        id = msg.getId();
        options = msg.getPollData().getOptionsList().stream().map(e -> new PollOption.Option(e.getLabel(), e.getCurrentTotal())).toList();
        option1 = new PollOption(new User(msg.getOptions1().getUser()), msg.getOptions1().getOptionsList().stream().map(e -> new PollOption.Option(e.getLabel(), e.getCurrentTotal())).toList());
        option2 = new PollOption(new User(msg.getOptions2().getUser()), msg.getOptions2().getOptionsList().stream().map(e -> new PollOption.Option(e.getLabel(), e.getCurrentTotal())).toList());
    }
}
