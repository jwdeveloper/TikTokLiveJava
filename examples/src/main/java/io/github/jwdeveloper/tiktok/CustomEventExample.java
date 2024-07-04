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
package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.data.models.gifts.*;
import lombok.AllArgsConstructor;

public class CustomEventExample {
    @AllArgsConstructor
    public static class CheapGiftEvent extends TikTokEvent {
        Gift gift;
    }

    @AllArgsConstructor
    public static class ExpensiveGiftEvent extends TikTokEvent {
        Gift gift;
    }

    public static void main(String[] args)
    {
        TikTokLive.newClient(ConnectionExample.TIKTOK_HOSTNAME)
                .configure(clientSettings ->
                {
                    clientSettings.setPrintToConsole(true);
                })
                .onGift((liveClient, event) ->
                {
                    if (event.getGift().getDiamondCost() > 100)
                        liveClient.publishEvent(new ExpensiveGiftEvent(event.getGift()));
                    else
                        liveClient.publishEvent(new CheapGiftEvent(event.getGift()));
                })
                .onEvent(CheapGiftEvent.class, (liveClient, event) ->
                {
                    System.out.println("Thanks for cheap gift");
                })
                .onEvent(ExpensiveGiftEvent.class, (liveClient, event) ->
                {
                    System.out.println("Thanks for expensive gift!");
                })
                .buildAndConnect();
    }
}