package io.github.jwdeveloper.tiktok.mappers.events;

import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.envelop.TikTokChestEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollEndEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollStartEvent;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollUpdateEvent;
import io.github.jwdeveloper.tiktok.data.events.room.TikTokRoomPinEvent;
import io.github.jwdeveloper.tiktok.data.models.chest.Chest;
import io.github.jwdeveloper.tiktok.messages.enums.EnvelopeDisplay;
import io.github.jwdeveloper.tiktok.messages.webcast.*;
import lombok.SneakyThrows;

import java.util.Collections;
import java.util.List;

public class TikTokCommonEventHandler
{

    @SneakyThrows
    public TikTokEvent handleWebcastControlMessage(byte[] msg) {
        var message = WebcastControlMessage.parseFrom(msg);
        return switch (message.getAction()) {
            case STREAM_PAUSED -> new TikTokLivePausedEvent();
            case STREAM_ENDED -> new TikTokLiveEndedEvent();
            case STREAM_UNPAUSED -> new TikTokLiveUnpausedEvent();
            default -> new TikTokUnhandledControlEvent(message);
        };
    }

    @SneakyThrows
    public TikTokEvent handlePinMessage(byte[] msg) {
        var pinMessage = WebcastRoomPinMessage.parseFrom(msg);
        var chatMessage = WebcastChatMessage.parseFrom(pinMessage.getPinnedMessage());
        var chatEvent = new TikTokCommentEvent(chatMessage);
        return new TikTokRoomPinEvent(pinMessage, chatEvent);
    }

    //TODO Probably not working
    @SneakyThrows
    public TikTokEvent handlePollEvent(byte[] msg) {
        var poolMessage = WebcastPollMessage.parseFrom(msg);
        return switch (poolMessage.getMessageType()) {
            case 0 -> new TikTokPollStartEvent(poolMessage);
            case 1 -> new TikTokPollEndEvent(poolMessage);
            case 2 -> new TikTokPollUpdateEvent(poolMessage);
            default -> new TikTokPollEvent(poolMessage);
        };
    }

    @SneakyThrows
    public List<TikTokEvent> handleEnvelop(byte[] data) {
        var msg = WebcastEnvelopeMessage.parseFrom(data);
        if (msg.getDisplay() != EnvelopeDisplay.EnvelopeDisplayNew) {
            return Collections.emptyList();
        }
        var totalDiamonds = msg.getEnvelopeInfo().getDiamondCount();
        var totalUsers = msg.getEnvelopeInfo().getPeopleCount();
        var chest = new Chest(totalDiamonds, totalUsers);

        return List.of(new TikTokChestEvent(chest, msg));
    }

}
