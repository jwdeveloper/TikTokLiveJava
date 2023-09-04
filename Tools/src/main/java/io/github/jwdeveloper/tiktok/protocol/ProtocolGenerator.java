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
