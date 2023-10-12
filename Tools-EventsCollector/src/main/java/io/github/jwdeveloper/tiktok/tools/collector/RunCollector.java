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
package io.github.jwdeveloper.tiktok.tools.collector;

import io.github.jwdeveloper.tiktok.messages.webcast.WebcastGiftMessage;
import io.github.jwdeveloper.tiktok.tools.collector.client.TikTokMessageCollectorClient;

import java.io.IOException;
import java.sql.SQLException;

public class RunCollector {

    //https://protobuf-decoder.netlify.app/
    //https://streamdps.com/tiktok-widgets/gifts/

    //WebcastLinkMicBattleItemCard does streamer win battle?

    public static void main(String[] args) throws SQLException, IOException {

        TikTokMessageCollectorClient.create("giftsCollector")
                .addUser("cbcgod")
                // .addUser("mr_cios")
               // .addUser("cbcgod")
                //   .addUser("psychotropnazywo")
                //  .addUser("accordionistka")
                .addEventFilter(WebcastGiftMessage.class)
                .addOnBuilder(liveClientBuilder ->
                {
                    liveClientBuilder.onGift((liveClient, event) ->
                    {

                    });

                    liveClientBuilder.onGiftCombo((liveClient, event) ->
                    {

                    });

                    liveClientBuilder.onGift((liveClient, event) ->
                    {
                       var sb = new StringBuilder();
                        sb.append("GIFT User: " + event.getUser().getDisplayName()+" ");
                        sb.append("Name: " + event.getGift().name() + " ");
                        sb.append("Combo: " + event.getCombo() + " ");
                        System.out.println(sb.toString());
                    });
                    liveClientBuilder.onGiftCombo((liveClient, event) ->
                    {
                        var sb = new StringBuilder();
                        sb.append("COMBO User: " + event.getUser().getDisplayName()+" ");
                        sb.append("Name: " + event.getGift().name() + " ");
                        sb.append("Combo: " + event.getCombo() + " ");
                        sb.append("Type: " + event.getComboState().name());
                        System.out.println(sb.toString());
                    });
                })
                .buildAndRun();

        System.in.read();
    }


}
