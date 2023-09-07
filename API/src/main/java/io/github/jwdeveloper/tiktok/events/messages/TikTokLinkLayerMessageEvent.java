package io.github.jwdeveloper.tiktok.events.messages;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import io.github.jwdeveloper.tiktok.events.base.TikTokHeaderEvent;
import io.github.jwdeveloper.tiktok.messages.WebcastLinkLayerMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@EventMeta(eventType = EventType.Message)
public class TikTokLinkLayerMessageEvent extends TikTokHeaderEvent {
    private final Long linkId;
    private final LinkData link1;
    private final LinkData link2;

    public TikTokLinkLayerMessageEvent(WebcastLinkLayerMessage msg) {
        super(msg.getHeader());
        linkId = msg.getId();
        link1 = new LinkData(msg.getIdContainer1().getIds().getId1(), msg.getIdContainer1().getIds().getId2());
        link2 = new LinkData(msg.getIdContainer2().getIds().getId1(), msg.getIdContainer2().getIds().getId2());
    }

    @AllArgsConstructor
    @Getter
    private class LinkData {
        private final Long id1;
        private final Long id2;
    }
}
