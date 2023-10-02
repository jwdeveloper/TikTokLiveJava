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
