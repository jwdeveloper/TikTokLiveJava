package io.github.jwdeveloper.tiktok;

import io.github.jwdeveloper.tiktok.annotations.EventMeta;
import io.github.jwdeveloper.tiktok.annotations.EventType;
import io.github.jwdeveloper.tiktok.events.TikTokEvent;
import org.reflections.Reflections;

import java.util.*;

public class EventsListGenerator
{


    //[a](https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/TikTokBarrageMessageEvent.java)
    //Message Events:
    //- [member](#member)

    public static String GetEventsList()
    {
        var classes =  getClasses();
        var builder = new StringBuilder();
        for(var entry : classes.entrySet())
        {
            builder.append(System.lineSeparator());
            builder.append(" **"+entry.getKey().name()+"**:").append(System.lineSeparator());

            for(var clazz : entry.getValue())
            {
                var name = clazz.getSimpleName();
                var baseUrl ="https://github.com/jwdeveloper/TikTok-Live-Java/blob/master/API/src/main/java/io/github/jwdeveloper/tiktok/events/messages/"+name+".java";
                builder.append("-  [").append(name).append("](").append(baseUrl).append(")").append(System.lineSeparator());
            }
        }

        return builder.toString();
    }


    private static Map<EventType, List<Class<?>>> getClasses()
    {
        Reflections reflections = new Reflections("io.github.jwdeveloper.tiktok.events.messages");
        var classes = reflections.getSubTypesOf(TikTokEvent.class).stream().toList();
        Map<EventType, List<Class<?>>> classMap = new HashMap<>();
        // Group classes by EventType
        for (Class<?> clazz : classes) {
            EventType eventType = getEventType(clazz);
            classMap.computeIfAbsent(eventType, k -> new ArrayList<>()).add(clazz);
        }
        return classMap;
    }

    private static EventType getEventType(Class<?> clazz) {
        EventMeta annotation = clazz.getAnnotation(EventMeta.class);
        if (annotation != null) {
            return annotation.eventType();
        }
        return EventType.Custom; // Default value if annotation not present
    }
    private class EventTypeComparator implements Comparator<Class<?>> {
        @Override
        public int compare(Class<?> class1, Class<?> class2) {
            EventType eventType1 = getEventType(class1);
            EventType eventType2 = getEventType(class2);
            return eventType1.compareTo(eventType2);
        }

        private EventType getEventType(Class<?> clazz) {
            EventMeta annotation = clazz.getAnnotation(EventMeta.class);
            if (annotation != null) {
                return annotation.eventType();
            }
            return EventType.Custom;
        }
    }
}
