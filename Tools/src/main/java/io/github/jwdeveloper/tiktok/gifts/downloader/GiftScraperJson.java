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


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.ArrayList;
import java.util.List;

public class GiftScraperJson {

    private final String baseUrl = "https://streamdps.com/tiktok-widgets/gifts/";


    public static void main(String[] args) {
        var instance = new GiftScraperJson();
        instance.run();
    }


    public List<GiftDto> run() {
        var mainPage = getPageContent(baseUrl);
        var countries = getCountriesLinks(mainPage);

        var allDocuments = getAllPagesDocuments(countries);
        allDocuments.add(mainPage);

        var output = new ArrayList<GiftDto>();
        for (var document : allDocuments) {
            var gifts = getGifts(document);
            output.addAll(gifts);
        }


        return output;
    }

    public List<Document> getAllPagesDocuments(List<String> pages) {
        List<Document> content = new ArrayList<>();
        for (var page : pages) {
            content.add(getPageContent(baseUrl + page));
        }
        return content;
    }

    public List<String> getCountriesLinks(Document document) {
        var output = new ArrayList<String>();
        var countriesElements = document.getElementsByTag("a");
        for (var element : countriesElements) {
            var value = element.attr("href");
            if (!value.contains("/tiktok-widgets/gifts/?")) {
                continue;
            }
            value = value.replace("/tiktok-widgets/gifts/", "");
            output.add(value);
        }
        return output;
    }

    public List<GiftDto> getGifts(Document document) {
        var container = document.getElementsByClass("section-block  bkg-charcoal");
        var giftsContainers = container.get(0).getElementsByClass("column width-1 center");

        var output = new ArrayList<GiftDto>();
        for (var giftContainer : giftsContainers) {
            var imageElement = giftContainer.getElementsByTag("img").get(0);
            var link = imageElement.attr("src");

            var coinsElement = giftContainer.getElementsByClass("color-white").get(0);
            var coins = coinsElement.text();

            var inputsElements = giftContainer.getElementsByTag("input");
            var idElement = inputsElements.get(0);
            var nameElement = inputsElements.get(1);

            var id = idElement.attr("value");
            var name = nameElement.attr("value");


            var gift = new GiftDto();
            gift.setImage(link);
            gift.setDiamondCost(Integer.parseInt(coins));
            gift.setId(Integer.parseInt(id));
            gift.setName(name);
            output.add(gift);
        }
        return output;
    }

    public Document getPageContent(String url) {
        try {
            var result = Jsoup.connect(url).get();
            System.out.println("Downloaded page: " + url);
            return result;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
