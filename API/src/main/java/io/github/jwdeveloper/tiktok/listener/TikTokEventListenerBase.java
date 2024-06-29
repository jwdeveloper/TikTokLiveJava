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
package io.github.jwdeveloper.tiktok.listener;

import io.github.jwdeveloper.tiktok.data.events.*;
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.events.control.TikTokPreConnectionEvent;
import io.github.jwdeveloper.tiktok.data.events.envelop.TikTokChestEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.*;
import io.github.jwdeveloper.tiktok.data.events.http.TikTokHttpResponseEvent;
import io.github.jwdeveloper.tiktok.data.events.link.*;
import io.github.jwdeveloper.tiktok.data.events.poll.TikTokPollEvent;
import io.github.jwdeveloper.tiktok.data.events.room.*;
import io.github.jwdeveloper.tiktok.data.events.social.*;
import io.github.jwdeveloper.tiktok.data.events.websocket.*;
import io.github.jwdeveloper.tiktok.live.LiveClient;

public abstract class TikTokEventListenerBase implements TikTokEventListener
{
	public void onUnhandledSocial(LiveClient client, TikTokUnhandledSocialEvent event) {}

	public void onChest(LiveClient client, TikTokChestEvent event) {}

	public void onLinkMicFanTicket(LiveClient client, TikTokLinkMicFanTicketEvent event) {}

	public void onEnvelope(LiveClient client, TikTokEnvelopeEvent event) {}

	public void onShop(LiveClient client, TikTokShopEvent event) {}

	public void onDetect(LiveClient client, TikTokDetectEvent event) {}

	public void onLinkLayer(LiveClient client, TikTokLinkLayerEvent event) {}

	public void onConnected(LiveClient client, TikTokConnectedEvent event) {}

	public void onPreConnection(LiveClient client, TikTokPreConnectionEvent event) {}

	public void onCaption(LiveClient client, TikTokCaptionEvent event) {}

	public void onQuestion(LiveClient client, TikTokQuestionEvent event) {}

	public void onRoomPin(LiveClient client, TikTokRoomPinEvent event) {}

	public void onRoomInfo(LiveClient client, TikTokRoomInfoEvent event) {}

	public void onLivePaused(LiveClient client, TikTokLivePausedEvent event) {}

	public void onLiveUnpaused(LiveClient client, TikTokLiveUnpausedEvent event) {}

	public void onLike(LiveClient client, TikTokLikeEvent event) {}

	public void onLink(LiveClient client, TikTokLinkEvent event) {}
	public void onLinkInvite(LiveClient client, TikTokLinkInviteEvent event) {}
	public void onLinkReply(LiveClient client, TikTokLinkReplyEvent event) {}
	public void onLinkCreate(LiveClient client, TikTokLinkCreateEvent event) {}
	public void onLinkClose(LiveClient client, TikTokLinkCloseEvent event) {}
	public void onLinkEnter(LiveClient client, TikTokLinkEnterEvent event) {}
	public void onLinkLeave(LiveClient client, TikTokLinkLeaveEvent event) {}
	public void onLinkCancel(LiveClient client, TikTokLinkCancelEvent event) {}
	public void onLinkKickOut(LiveClient client, TikTokLinkKickOutEvent event) {}
	public void onLinkLinkedListChange(LiveClient client, TikTokLinkLinkedListChangeEvent event) {}
	public void onLinkUpdateUser(LiveClient client, TikTokLinkUpdateUserEvent event) {}
	public void onLinkWaitListChange(LiveClient client, TikTokLinkWaitListChangeEvent event) {}
	public void onLinkMute(LiveClient client, TikTokLinkMuteEvent event) {}
	public void onLinkRandomMatch(LiveClient client, TikTokLinkRandomMatchEvent event) {}
	public void onLinkUpdateUserSettings(LiveClient client, TikTokLinkUpdateUserSettingEvent event) {}
	public void onLinkMicIdxUpdate(LiveClient client, TikTokLinkMicIdxUpdateEvent event) {}
	public void onLinkListChange(LiveClient client, TikTokLinkListChangeEvent event) {}
	public void onLinkCohostListChange(LiveClient client, TikTokLinkCohostListChangeEvent event) {}
	public void onLinkMediaChange(LiveClient client, TikTokLinkMediaChangeEvent event) {}
	public void onLinkAcceptNotice(LiveClient client, TikTokLinkAcceptNoticeEvent event) {}
	public void onLinkSysKickOut(LiveClient client, TikTokLinkSysKickOutEvent event) {}
	public void onLinkUserToast(LiveClient client, TikTokLinkUserToastEvent event) {}

	public void onBarrage(LiveClient client, TikTokBarrageEvent event) {}

	public void onGift(LiveClient client, TikTokGiftEvent event) {}

	public void onGiftCombo(LiveClient client, TikTokGiftComboEvent event) {}

	public void onLinkMicArmies(LiveClient client, TikTokLinkMicArmiesEvent event) {}

	public void onEmote(LiveClient client, TikTokEmoteEvent event) {}

	public void onUnauthorizedMember(LiveClient client, TikTokUnauthorizedMemberEvent event) {}

	public void onInRoomBanner(LiveClient client, TikTokInRoomBannerEvent event) {}

	public void onLinkMicMethod(LiveClient client, TikTokLinkMicMethodEvent event) {}

	public void onSubscribe(LiveClient client, TikTokSubscribeEvent event) {}

	public void onPoll(LiveClient client, TikTokPollEvent event) {}

	public void onFollow(LiveClient client, TikTokFollowEvent event) {}

	public void onComment(LiveClient client, TikTokCommentEvent event) {}

	public void onHttpResponse(LiveClient client, TikTokHttpResponseEvent action) {}

	public void onGoalUpdate(LiveClient client, TikTokGoalUpdateEvent event) {}

	public void onRankUpdate(LiveClient client, TikTokRankUpdateEvent event) {}

	public void onIMDelete(LiveClient client, TikTokIMDeleteEvent event) {}

	public void onLiveEnded(LiveClient client, TikTokLiveEndedEvent event) {}

	public void onError(LiveClient client, TikTokErrorEvent event) {}

	public void onJoin(LiveClient client, TikTokJoinEvent event) {}

	public void onRankText(LiveClient client, TikTokRankTextEvent event) {}

	public void onShare(LiveClient client, TikTokShareEvent event) {}

	public void onUnhandledMember(LiveClient client, TikTokUnhandledMemberEvent event) {}

	public void onSubNotify(LiveClient client, TikTokSubNotifyEvent event) {}

	public void onLinkMicBattle(LiveClient client, TikTokLinkMicBattleEvent event) {}

	public void onDisconnected(LiveClient client, TikTokDisconnectedEvent event) {}

	public void onUnhandledControl(LiveClient client, TikTokUnhandledControlEvent event) {}

	public void onEvent(LiveClient client, TikTokEvent event) {}

	public void onWebsocketResponse(LiveClient client, TikTokWebsocketResponseEvent event) {}

	public void onWebsocketMessage(LiveClient client, TikTokWebsocketMessageEvent event) {}

	public void onWebsocketUnhandledMessage(LiveClient client, TikTokWebsocketUnhandledMessageEvent event) {}

	public void onReconnecting(LiveClient client, TikTokReconnectingEvent event) {}
}