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
package io.github.jwdeveloper.tiktok.mappers;

import io.github.jwdeveloper.dependance.api.DependanceContainer;
import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.link.*;
import io.github.jwdeveloper.tiktok.mappers.data.MappingResult;
import io.github.jwdeveloper.tiktok.mappers.handlers.TikTokCommonEventHandler;
import io.github.jwdeveloper.tiktok.mappers.handlers.TikTokGiftEventHandler;
import io.github.jwdeveloper.tiktok.mappers.handlers.TikTokRoomInfoEventHandler;
import io.github.jwdeveloper.tiktok.mappers.handlers.TikTokSocialMediaEventHandler;
import io.github.jwdeveloper.tiktok.messages.webcast.*;

import static io.github.jwdeveloper.tiktok.messages.enums.LinkMessageType.*;

public class MessagesMapperFactory {
    public static TikTokLiveMapper create(DependanceContainer container) {

        var helper = container.find(LiveMapperHelper.class);
        var mapper = new TikTokLiveMapper(helper);

        //ConnectionEvents events
        var commonHandler = container.find(TikTokCommonEventHandler.class);
        var giftHandler = container.find(TikTokGiftEventHandler.class);
        var roomInfoHandler = container.find(TikTokRoomInfoEventHandler.class);
        var socialHandler = container.find(TikTokSocialMediaEventHandler.class);


        mapper.forMessage(WebcastControlMessage.class, commonHandler::handleWebcastControlMessage);

        //Room status events
        mapper.forMessage(WebcastLiveIntroMessage.class, roomInfoHandler::handleIntro);
        mapper.forMessage(WebcastRoomUserSeqMessage.class, roomInfoHandler::handleUserRanking);
        mapper.forMessage(WebcastCaptionMessage.class, (inputBytes, messageName, mapperHelper) ->
        {
            var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, WebcastCaptionMessage.class);
            return MappingResult.of(messageObject, new TikTokCaptionEvent(messageObject));
        });


        //User Interactions events
        mapper.forMessage(WebcastChatMessage.class, (inputBytes, messageName, mapperHelper) ->
        {
            var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, WebcastChatMessage.class);
            return MappingResult.of(messageObject, new TikTokCommentEvent(messageObject));
        });
        mapper.forMessage(WebcastSubNotifyMessage.class, (inputBytes, messageName, mapperHelper) ->
        {
            var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, WebcastSubNotifyMessage.class);
            return MappingResult.of(messageObject, new TikTokSubscribeEvent(messageObject));
        });
        mapper.forMessage(WebcastEmoteChatMessage.class, (inputBytes, messageName, mapperHelper) ->
        {
            var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, WebcastEmoteChatMessage.class);
            return MappingResult.of(messageObject, new TikTokEmoteEvent(messageObject));
        });
        mapper.forMessage(WebcastQuestionNewMessage.class, (inputBytes, messageName, mapperHelper) ->
        {
            var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, WebcastQuestionNewMessage.class);
            return MappingResult.of(messageObject, new TikTokQuestionEvent(messageObject));
        });

        mapper.forMessage(WebcastLikeMessage.class, roomInfoHandler::handleLike);
        mapper.forMessage(WebcastGiftMessage.class, giftHandler::handleGifts);
        mapper.forMessage(WebcastSocialMessage.class, socialHandler::handle);
        mapper.forMessage(WebcastMemberMessage.class, roomInfoHandler::handleMemberMessage);

        //Host Interaction events
        mapper.forMessage(WebcastPollMessage.class, commonHandler::handlePollEvent);
        mapper.forMessage(WebcastRoomPinMessage.class, commonHandler::handlePinMessage);
        mapper.forMessage(WebcastChatMessage.class, (inputBytes, messageName, mapperHelper) ->
        {
            var messageObject = mapperHelper.bytesToWebcastObject(inputBytes, WebcastChatMessage.class);
            return MappingResult.of(messageObject, new TikTokCommentEvent(messageObject));
        });

        //LinkMic events
        mapper.forMessage(WebcastLinkMicBattle.class, (inputBytes, messageName, mapperHelper) -> {
            var message = mapperHelper.bytesToWebcastObject(inputBytes, WebcastLinkMicBattle.class);
            return MappingResult.of(message, new TikTokLinkMicBattleEvent(message));
        });
        mapper.forMessage(WebcastLinkMicArmies.class, (inputBytes, messageName, mapperHelper) -> {
            var message = mapperHelper.bytesToWebcastObject(inputBytes, WebcastLinkMicArmies.class);
            return MappingResult.of(message, new TikTokLinkMicArmiesEvent(message));
        });
        mapper.forMessage(WebcastLinkMessage.class, ((inputBytes, messageName, mapperHelper) -> {
            var message = mapperHelper.bytesToWebcastObject(inputBytes, WebcastLinkMessage.class);
            return MappingResult.of(message, switch (message.getMessageType()) {
                case TYPE_LINKER_INVITE -> new TikTokLinkInviteEvent(message);
                case TYPE_LINKER_CREATE -> new TikTokLinkCreateEvent(message);
                case TYPE_LINKER_CLOSE -> new TikTokLinkCloseEvent(message);
                case TYPE_LINKER_ENTER -> new TikTokLinkEnterEvent(message);
                case TYPE_LINKER_LEAVE -> new TikTokLinkLeaveEvent(message);
                case TYPE_LINKER_CANCEL_INVITE, TYPE_LINKER_CANCEL_APPLY -> new TikTokLinkCancelEvent(message);
                case TYPE_LINKER_KICK_OUT -> new TikTokLinkKickOutEvent(message);
                case TYPE_LINKER_LINKED_LIST_CHANGE -> new TikTokLinkLinkedListChangeEvent(message);
                case TYPE_LINKER_UPDATE_USER -> new TikTokLinkUpdateUserEvent(message);
                case TYPE_LINKER_WAITING_LIST_CHANGE, TYPE_LINKER_WAITING_LIST_CHANGE_V2 ->
                        new TikTokLinkWaitListChangeEvent(message);
                case TYPE_LINKER_MUTE -> new TikTokLinkMuteEvent(message);
                case TYPE_LINKER_MATCH -> new TikTokLinkRandomMatchEvent(message);
                case TYPE_LINKER_UPDATE_USER_SETTING -> new TikTokLinkUpdateUserSettingEvent(message);
                case TYPE_LINKER_MIC_IDX_UPDATE -> new TikTokLinkMicIdxUpdateEvent(message);
                case TYPE_LINKER_LINKED_LIST_CHANGE_V2 -> new TikTokLinkListChangeEvent(message);
                case TYPE_LINKER_COHOST_LIST_CHANGE -> new TikTokLinkCohostListChangeEvent(message);
                case TYPE_LINKER_MEDIA_CHANGE -> new TikTokLinkMediaChangeEvent(message);
                case TYPE_LINKER_ACCEPT_NOTICE -> new TikTokLinkAcceptNoticeEvent(message);
                case TYPE_LINKER_SYS_KICK_OUT -> new TikTokLinkSysKickOutEvent(message);
                case TYPE_LINKMIC_USER_TOAST -> new TikTokLinkUserToastEvent(message);
                default -> new TikTokLinkEvent(message);
            });
        }));
        // mapper.webcastObjectToConstructor(WebcastLinkMicMethod.class, TikTokLinkMicMethodEvent.class);
        //  mapper.webcastObjectToConstructor(WebcastLinkMicFanTicketMethod.class, TikTokLinkMicFanTicketEvent.class);

        //Rank events
        //   mapper.webcastObjectToConstructor(WebcastRankTextMessage.class, TikTokRankTextEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastRankUpdateMessage.class, TikTokRankUpdateEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastHourlyRankMessage.class, TikTokRankUpdateEvent.class);

        //Others events
        //  mapper.webcastObjectToConstructor(WebcastInRoomBannerMessage.class, TikTokInRoomBannerEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastMsgDetectMessage.class, TikTokDetectEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastBarrageMessage.class, TikTokBarrageEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastUnauthorizedMemberMessage.class, TikTokUnauthorizedMemberEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastOecLiveShoppingMessage.class, TikTokShopEvent.class);
        //   mapper.webcastObjectToConstructor(WebcastImDeleteMessage.class, TikTokIMDeleteEvent.class);
        //  mapper.bytesToEvents(WebcastEnvelopeMessage.class, commonHandler::handleEnvelop);
        return mapper;
    }
}
