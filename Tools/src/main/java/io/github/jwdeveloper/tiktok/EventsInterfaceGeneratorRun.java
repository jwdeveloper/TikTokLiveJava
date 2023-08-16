package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.events_generator.EventGeneratorSettings;
import io.github.jwdeveloper.tiktok.intefacee.EventsInterfaceGenerator;

import java.io.IOException;

public class EventsInterfaceGeneratorRun
{
    public static void main(String[] args) throws IOException {
        var settings = new EventGeneratorSettings();
        settings.setTikTokEvent(true);
        settings.setPrefix("TikTok");
        settings.setEndFix("Event");
        settings.setInputDictionary("C:\\Users\\ja\\RiderProjects\\TikTokLiveSharp\\TikTokLiveSharp\\Events\\Messages");
        settings.setOutputDictionary("C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\API\\src\\main\\java\\io\\github\\jwdeveloper\\tiktok\\events\\messages");
        var generator = new EventsInterfaceGenerator();
        generator.compile(settings);
    }
}
