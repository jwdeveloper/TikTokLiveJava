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
package io.github.jwdeveloper.tiktok.gifts.downloader;

import io.github.jwdeveloper.tiktok.TikTokLive;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.utils.FilesUtility;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class GiftOfficialJson {

    public static void main(String[] args) {
        new GiftOfficialJson().run();
    }

    public List<GiftDto> run() {
        try {
            var dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            var now = LocalDateTime.now();
            var date = now.format(dtf).replace("/", "_");
            var fileName = "official_" + date + ".json";

            var httpClient = TikTokLive.requests();
            var giftsInfo = httpClient.fetchGiftsData();
            FilesUtility.saveFile("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Tools\\src\\main\\resources\\gifts\\official\\" + fileName, giftsInfo.getJson());


            return giftsInfo.getGifts().stream().map(e ->
            {
                var gift = new GiftDto();
                gift.setId(e.getId());
                gift.setImage(e.getImage());
                gift.setName(e.getName());
                gift.setDiamondCost(e.getDiamondCost());
                return gift;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Failed to fetch giftTokens from WebCast, see stacktrace for more info.", e);
        }
    }


}
