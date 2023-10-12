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
package io.github.jwdeveloper.tiktok.protocol;


import org.jsoup.Jsoup;
import java.io.File;
import java.io.IOException;

public class ProtocolGenerator
{
    public static void main(String[] args) {
        // Path to the HTML file
        File htmlFile = new File("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Tools\\src\\main\\resources\\page.html");

        try {
            // Parse the HTML file with Jsoup
            var doc = Jsoup.parse(htmlFile, "UTF-8");

            // Find all script tags
            var scriptTags = doc.select("script");

            // Display all script tags
            int counter = 1;
            for (var scriptTag : scriptTags) {
                String srcValue = scriptTag.attr("src");



                if(!srcValue.contains("tiktok/webapp/main/webapp-live/"))
                {
                    continue;
                }
                // Only print those script tags which have a 'src' attribute
                if (!srcValue.isEmpty()) {
                    System.out.println("Script Tag " + counter + " src attribute: " + srcValue);
                }
                counter++;
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
