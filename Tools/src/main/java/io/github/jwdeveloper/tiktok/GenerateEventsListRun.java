package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import org.reflections.Reflections;

public class GenerateEventsListRun
{


    //[a](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokBarrageMessageEvent.java)
    //Message Events:
    //- [member](#member)

    public static void main(String[] args)
    {
        Reflections reflections = new Reflections("io.github.jwdeveloper.tiktok.events.messages");
        var classes = reflections.getSubTypesOf(TikTokEvent.class);
        classes.add(TikTokEvent.class);

        var builder = new StringBuilder();
        builder.append("Events:").append(System.lineSeparator());
        for(var event : classes)
        {
            var name = event.getSimpleName();
            var baseUrl ="https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/"+name+".java";
            builder.append("-  [").append(name).append("](").append(baseUrl).append(")").append(System.lineSeparator());
        }

        System.out.println(builder.toString());
    }
}
