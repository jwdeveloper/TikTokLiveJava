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

import io.github.jwdeveloper.tiktok.data.events.TikTokCommentEvent;
import io.github.jwdeveloper.tiktok.data.events.TikTokSubscribeEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftComboEvent;
import io.github.jwdeveloper.tiktok.data.events.gift.TikTokGiftEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokFollowEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokJoinEvent;
import io.github.jwdeveloper.tiktok.data.events.social.TikTokLikeEvent;
import io.github.jwdeveloper.tiktok.data.models.gifts.GiftComboStateType;
import io.github.jwdeveloper.tiktok.live.LiveClient;

public class Events_And_Gifts_Testing_Example
{

    public static void main(String[] args) {
        LiveClient client = TikTokLive.newClient(ConnectionExample.TIKTOK_HOSTNAME)
                .configure(liveClientSettings ->
                {
                    liveClientSettings.setOffline(true);
                    liveClientSettings.setPrintToConsole(true);
                })
                .onConnected((liveClient, event) ->
                {
                    liveClient.getLogger().info("Connected");
                })
                .onDisconnected((liveClient, event) ->
                {
                    liveClient.getLogger().info("Disconnected");
                })
                .onGiftCombo((liveClient, event) ->
                {
                    liveClient.getLogger().info("New fake combo Gift: " + event.getGift());
                })
                .onGift((liveClient, event) ->
                {
                    liveClient.getLogger().info("New fake Gift: " + event.getGift());
                })
                .onLike((liveClient, event) ->
                {
                    liveClient.getLogger().info("New fake Like event: " + event.getLikes());
                })
                .build();

        var gifts = TikTokLive.gifts();
        var roseGift = gifts.getByName("Rose");

        var fakeGift = TikTokGiftEvent.of(roseGift);
        var fakeComboGift = TikTokGiftComboEvent.of(roseGift, 12, GiftComboStateType.Begin);
        var fakeMessage = TikTokCommentEvent.of("Mark", "Hello world");
        var fakeSubscriber = TikTokSubscribeEvent.of("Mark");
        var fakeFollow = TikTokFollowEvent.of("Mark");
        var fakeLike = TikTokLikeEvent.of("Mark", 12);
        var fakeJoin = TikTokJoinEvent.of("Mark");

        client.connect();

        client.publishEvent(fakeGift);
        client.publishEvent(fakeComboGift);
        client.publishEvent(fakeMessage);
        client.publishEvent(fakeSubscriber);
        client.publishEvent(fakeFollow);
        client.publishEvent(fakeJoin);

        client.publishEvent(fakeLike);
        client.publishMessage("WebcastLikeMessage", webcastLikeMessageBase64);

        client.disconnect();
    }

    private static final String webcastLikeMessageBase64 = "SAFSBRABGKwCUgcIAhABGKwCCv8BUAFYAbABA7gBARCflqWWo8Ha72UgzoPZhd8xQrwBGg4gkAMKCSNmZmZmZmZmZiJ/qgF6CngIhYjjgPWJv7RgGhDwnZKm8J2TjvCdk47wk4WTsgIKa3lsbGVlaGFsbPICTE1TNHdMakFCQUFBQXUyX21LNEw4WGJYa3lNaUFvZzJUTnNmVjk5N09WM2tpQ3NCTkNjYWkwcWxIcUt0Q3B0UGU1N2RLYVhxb0xWSXoICwoQcG1fbXRfbXNnX3ZpZXdlchIXezA6dXNlcn0gbGlrZWQgdGhlIExJVkVIAQoSV2ViY2FzdExpa2VNZXNzYWdlGIaWvY+RhdjvZTABwAEBEA8Y+Voq7RCyAQYImwEQjwK6AQCCAgDyAkxNUzR3TGpBQkFBQUF1Ml9tSzRMOFhiWGt5TWlBb2cyVE5zZlY5OTdPVjNraUNzQk5DY2FpMHFsSHFLdENwdFBlNTdkS2FYcW9MVkl6ggTqCLoBnwUqBggBEAEYIFoNCgASCSNCMzQ3N0VGRoABDwgEEtgEEix3ZWJjYXN0LXZhL2dyYWRlX2JhZGdlX2ljb25fbGl0ZV9sdjE1X3YyLnBuZzrpAnNzbG9jYWw6Ly93ZWJjYXN0X2x5bnh2aWV3X3BvcHVwP3VzZV9zcGFyaz0xJnVybD1odHRwcyUzQSUyRiUyRmxmMTYtZ2Vja28tc291cmNlLnRpa3Rva2Nkbi5jb20lMkZvYmolMkZieXRlLWd1cmQtc291cmNlLXNnJTJGdGlrdG9rJTJGZmUlMkZsaXZlJTJGdGlrdG9rX2xpdmVfcmV2ZW51ZV91c2VyX2xldmVsX21haW4lMkZzcmMlMkZwYWdlcyUyRnByaXZpbGVnZSUyRnBhbmVsJTJGdGVtcGxhdGUuanMmaGlkZV9zdGF0dXNfYmFyPTAmaGlkZV9uYXZfYmFyPTEmY29udGFpbmVyX2JnX2NvbG9yPTAwMDAwMDAwJmhlaWdodD05MCUyNSZiZGhtX2JpZD10aWt0b2tfbGl2ZV9yZXZlbnVlX3VzZXJfbGV2ZWxfbWFpbiZ1c2VfZm9yZXN0PTEKXWh0dHBzOi8vcDE2LXdlYmNhc3QudGlrdG9rY2RuLmNvbS93ZWJjYXN0LXZhL2dyYWRlX2JhZGdlX2ljb25fbGl0ZV9sdjE1X3YyLnBuZ350cGx2LW9iai5pbWFnZQpdaHR0cHM6Ly9wMTktd2ViY2FzdC50aWt0b2tjZG4uY29tL3dlYmNhc3QtdmEvZ3JhZGVfYmFkZ2VfaWNvbl9saXRlX2x2MTVfdjIucG5nfnRwbHYtb2JqLmltYWdlIgIxNTIAOgYaAhIAIgBiDQoAEgkjQjM0NzdFRkZ4DqIBBggBEAEYIAgEEBQYCCABUukCc3Nsb2NhbDovL3dlYmNhc3RfbHlueHZpZXdfcG9wdXA/dXNlX3NwYXJrPTEmdXJsPWh0dHBzJTNBJTJGJTJGbGYxNi1nZWNrby1zb3VyY2UudGlrdG9rY2RuLmNvbSUyRm9iaiUyRmJ5dGUtZ3VyZC1zb3VyY2Utc2clMkZ0aWt0b2slMkZmZSUyRmxpdmUlMkZ0aWt0b2tfbGl2ZV9yZXZlbnVlX3VzZXJfbGV2ZWxfbWFpbiUyRnNyYyUyRnBhZ2VzJTJGcHJpdmlsZWdlJTJGcGFuZWwlMkZ0ZW1wbGF0ZS5qcyZoaWRlX3N0YXR1c19iYXI9MCZoaWRlX25hdl9iYXI9MSZjb250YWluZXJfYmdfY29sb3I9MDAwMDAwMDAmaGVpZ2h0PTkwJTI1JmJkaG1fYmlkPXRpa3Rva19saXZlX3JldmVudWVfdXNlcl9sZXZlbF9tYWluJnVzZV9mb3Jlc3Q9MVgBYk8qAjE1CgEyEhM3MTM4MzgxNzQ3MjkyNTQyNzU2GgEwIi5tb2NrX2ZpeF93aWR0aF90cmFuc3BhcmVudF83MTM4MzgxNzQ3MjkyNTQyNzU2CIWI44D1ib+0YBoQ8J2SpvCdk47wnZOO8JOFk0r1BhJBMTAweDEwMC90b3MtdXNlYXN0OC1hdnQtMDA2OC10eDIvNjY0NmM4NjZjMzI1MWEwOTY3NjhiYjY4OTUyODVjMzEK0gFodHRwczovL3AxOS1wdS1zaWduLXVzZWFzdDgudGlrdG9rY2RuLXVzLmNvbS90b3MtdXNlYXN0OC1hdnQtMDA2OC10eDIvNjY0NmM4NjZjMzI1MWEwOTY3NjhiYjY4OTUyODVjMzF+dHBsdi10aWt0b2stc2hyaW5rOjcyOjcyLndlYnA/bGszcz1hNWQ0ODA3OCZ4LWV4cGlyZXM9MTcwOTMxMjQwMCZ4LXNpZ25hdHVyZT1VMlNEbUk3Z3R5RW9rMlBlWFdmeTNsM1F6NlElM0QKyAFodHRwczovL3AxNi1wdS1zaWduLXVzZWFzdDgudGlrdG9rY2RuLXVzLmNvbS90b3MtdXNlYXN0OC1hdnQtMDA2OC10eDIvNjY0NmM4NjZjMzI1MWEwOTY3NjhiYjY4OTUyODVjMzF+YzVfMTAweDEwMC53ZWJwP2xrM3M9YTVkNDgwNzgmeC1leHBpcmVzPTE3MDkzMTI0MDAmeC1zaWduYXR1cmU9aWNWZEVZa0FnWkYlMkZ2WU5OTSUyRlVNMzE2eG9HdyUzRArGAWh0dHBzOi8vcDE5LXB1LXNpZ24tdXNlYXN0OC50aWt0b2tjZG4tdXMuY29tL3Rvcy11c2Vhc3Q4LWF2dC0wMDY4LXR4Mi82NjQ2Yzg2NmMzMjUxYTA5Njc2OGJiNjg5NTI4NWMzMX5jNV8xMDB4MTAwLndlYnA/bGszcz1hNWQ0ODA3OCZ4LWV4cGlyZXM9MTcwOTMxMjQwMCZ4LXNpZ25hdHVyZT1PQzdBQ3htQUklMkJsYlp4RkVuWktJT1RyRExGUSUzRArGAWh0dHBzOi8vcDE2LXB1LXNpZ24tdXNlYXN0OC50aWt0b2tjZG4tdXMuY29tL3Rvcy11c2Vhc3Q4LWF2dC0wMDY4LXR4Mi82NjQ2Yzg2NmMzMjUxYTA5Njc2OGJiNjg5NTI4NWMzMX5jNV8xMDB4MTAwLmpwZWc/bGszcz1hNWQ0ODA3OCZ4LWV4cGlyZXM9MTcwOTMxMjQwMCZ4LXNpZ25hdHVyZT02YUwlMkZNZWtOeHg5NXlvVTVLOTZON0xwRUlNdyUzRLICCmt5bGxlZWhhbGxCyQEIgojG1pKb0clgErwBChBwbV9tdF9tc2dfdmlld2VyEhd7MDp1c2VyfSBsaWtlZCB0aGUgTElWRRoOCgkjZmZmZmZmZmYgkAMifwgLqgF6CngIhYjjgPWJv7RgGhDwnZKm8J2TjvCdk47wk4WTsgIKa3lsbGVlaGFsbPICTE1TNHdMakFCQUFBQXUyX21LNEw4WGJYa3lNaUFvZzJUTnNmVjk5N09WM2tpQ3NCTkNjYWkwcWxIcUt0Q3B0UGU1N2RLYVhxb0xWSXo=";
}
