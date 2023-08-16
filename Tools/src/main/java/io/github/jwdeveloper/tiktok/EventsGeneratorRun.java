package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.events_generator.EventGeneratorSettings;
import io.github.jwdeveloper.tiktok.events_generator.EventsGenerator;

import java.io.IOException;

public class EventsGeneratorRun {

    private static boolean lock = false;

    //Run objects
    public static void main(String[] args) throws IOException {

        if(lock)
        {
            return;
        }
        //generatesObjects()
       // generateEventsMessages();
    }


    private static void generatesEventsObjects() throws IOException {
        var settings = new EventGeneratorSettings();
        settings.setTikTokEvent(false);
        settings.setInputDictionary("C:\\Users\\ja\\RiderProjects\\TikTokLiveSharp\\TikTokLiveSharp\\Events\\Objects");
        settings.setOutputDictionary("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\API\\src\\main\\java\\io\\github\\jwdeveloper\\tiktok\\events\\objects");
        var generator = new EventsGenerator();
        generator.compile(settings);
    }

    private static void generateEventsMessages() throws IOException {
        var settings = new EventGeneratorSettings();
        settings.setTikTokEvent(true);
        settings.setPrefix("TikTok");
        settings.setEndFix("Event");
        settings.setInputDictionary("C:\\Users\\ja\\RiderProjects\\TikTokLiveSharp\\TikTokLiveSharp\\Events\\Messages");
        settings.setOutputDictionary("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\API\\src\\main\\java\\io\\github\\jwdeveloper\\tiktok\\events\\messages");
        var generator = new EventsGenerator();
        generator.compile(settings);
    }


}
