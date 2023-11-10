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
package io.github.jwdeveloper.tiktok.gifts;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.github.jwdeveloper.tiktok.gifts.downloader.GiftDto;
import io.github.jwdeveloper.tiktok.gifts.downloader.GiftExtraJson;
import io.github.jwdeveloper.tiktok.gifts.downloader.GiftOfficialJson;
import io.github.jwdeveloper.tiktok.gifts.downloader.GiftScraperJson;
import io.github.jwdeveloper.tiktok.utils.FilesUtility;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class GiftsDownloader {

    public static void main(String[] run) {
        var gifts = new GiftsDownloader().getGifts();
        for(var gift : gifts)
        {
            System.out.println(gift.toString());
        }
    }

    public List<GiftDto> getGiftsFromFile() {
        var content = FilesUtility.loadFileContent("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Tools\\src\\main\\resources\\gifts\\output_1_0_4.json");
        Type mapType = new TypeToken<Map<Integer, GiftDto>>() {
        }.getType();
        var mapper = new Gson().fromJson(content, mapType);

        var gifts = (Map<Integer, GiftDto>) mapper;
        return gifts.values().stream().toList();
    }

    public List<GiftDto> getGifts() {
        var scraper = new GiftScraperJson();
        System.out.println("Downlooading Scraped Gifts");
        var scraperGifts = scraper.run();
        System.out.println("Scraped Gifts: " + scraperGifts.size());

        System.out.println("Downlooading Official Gifts");
        var officalGift = new GiftOfficialJson();
        var officialGifts = officalGift.run();
        System.out.println("Official Gifts: " + officialGifts.size());

        System.out.println("Downlooading Official Gifts");
        var extraGiftsJson = new GiftExtraJson();
        var extraGifts = extraGiftsJson.run();
        System.out.println("Official Gifts: " + extraGifts.size());

        var outputHashMap = new TreeMap<Integer, GiftDto>();
        for (var gift : scraperGifts) {
            outputHashMap.put(gift.getId(), gift);
        }
        for (var gift : officialGifts) {
            outputHashMap.put(gift.getId(), gift);
        }
        for (var gift : extraGifts) {
            outputHashMap.put(gift.getId(), gift);
        }
        var gson = new GsonBuilder().setPrettyPrinting()
                .create();
        var json = gson.toJson(outputHashMap);
        FilesUtility.saveFile("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Tools\\src\\main\\resources\\gifts\\output_1_0_4.json", json);
        System.out.println("Gifts saved to file!");
        return outputHashMap.values().stream().toList();
    }
}
