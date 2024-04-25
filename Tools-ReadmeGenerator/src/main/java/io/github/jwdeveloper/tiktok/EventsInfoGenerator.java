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
import io.github.jwdeveloper.tiktok.data.events.common.TikTokEvent;
import io.github.jwdeveloper.tiktok.live.builder.EventsBuilder;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventsInfoGenerator {
    public static void main(String[] args) throws ClassNotFoundException {
        String res = new EventsInfoGenerator().run();
        System.out.println(res);
    }


    public String run() {
        Map<EventType, List<EventDto>> events = getEventsDtos();
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<EventType, List<EventDto>> entry : events.entrySet()) {
            builder.append(System.lineSeparator());
            builder.append(System.lineSeparator());
            builder.append(" **" + entry.getKey().name() + "**:").append(System.lineSeparator());
            for (EventDto dto : entry.getValue()) {
                StringBuilder link = getLink(dto);
                builder.append(System.lineSeparator());
                builder.append(link);
            }
        }
        builder.append(System.lineSeparator());
        builder.append("# Examples");
        builder.append(System.lineSeparator());
        for (Map.Entry<EventType, List<EventDto>> entry : events.entrySet()) {
            for (EventDto dto : entry.getValue()) {
                builder.append("<br>");
                builder.append(getMethodContent(dto));
            }
        }

        return builder.toString();
    }

    public StringBuilder getLink(EventDto dto) {
        StringBuilder sb = new StringBuilder();
        String name = dto.getMethodName().toLowerCase()+"-"+dto.getEventClazz().getSimpleName().toLowerCase();
        sb.append("- [").append(dto.getMethodName()).append("](#").append(name).append(")");
        return sb;
    }

    public String getMethodContent(EventDto dto) {
        Map<String, Object> variables = new HashMap<String, Object>();
        String doc = getClazzDocumentation(dto.getEventClazz());
        variables.put("method-name", dto.getMethodName());
        variables.put("content", doc);
        variables.put("event-name", dto.getEventClazz().getSimpleName());

        String baseUrl = "https://github.com/jwdeveloper/TikTokLiveJava/blob/master/API/src/main/java/";
        baseUrl += dto.getEventClazz().getPackage().getName().replace(".","/");
        baseUrl += "/"+dto.getEventClazz().getSimpleName()+".java";
        variables.put("event-file-url",baseUrl);
        String temp = "\"" +
                "\n" +
                "\n" +
                "## {{method-name}} [{{event-name}}]({{event-file-url}})" +
                "\n" +
                "{{content}}" +
                "\n" +
                "```java" +
                "TikTokLive.newClient(\"host-name\")" +
                ".{{method-name}}((liveClient, event) ->" +
                "{" +
                "\n" +
                "})" +
                ".buildAndConnect();" +
                "```" +
                "\n" +
                "\n" +
                "\n" +
                "\"";
        return TemplateUtility.generateTemplate(temp, variables);
    }

    public String getClazzDocumentation(Class<?> clazz) {
        String path = clazz.getName();
        path = path.replace(".", "\\");
        String fullPath = "C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\API\\src\\main\\java\\" + path + ".java";
        String content = FilesUtility.loadFileContent(fullPath);
        int index = content.indexOf(" */");
        content = content.substring(index+4);

        String pattern = "(?s)\\/\\\\*(.*?)\\*\\/";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(content);



        if (!m.find()) {
            return "";
        }

        String group = m.group(1);
        String reuslt = group.replace("*","").replaceAll("\\*", "");
        return reuslt;
    }

    public Map<EventType, List<EventDto>> getEventsDtos(){

        Map<EventType, List<EventDto>> result = new TreeMap<EventType, List<EventDto>>();
        Class<EventsBuilder> baseClazz = EventsBuilder.class;
        Reflections reflections = new Reflections("io.github.jwdeveloper.tiktok.data.events");
        Set<Class<? extends TikTokEvent>> classes = reflections.getSubTypesOf(TikTokEvent.class);
        Method[] methods = baseClazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals("onEvent")) {
                EventDto dto = new EventDto(EventType.Message, "onEvent", TikTokEvent.class);
                result.computeIfAbsent(EventType.Message, eventType -> new ArrayList<>()).add(dto);
                continue;
            }

            String parsedName = method.getName().replaceFirst("on", "");
            String name = "TikTok" + parsedName + "Event";
            Optional<Class<? extends TikTokEvent>> optional = classes.stream().filter(e -> e.getSimpleName().equals(name)).findFirst();
            if (!optional.isPresent()) {
                System.out.println("Not found!: " + name);
                continue;
            }
            Class<? extends TikTokEvent> clazz = optional.get();
            EventMeta annotation = clazz.getAnnotation(EventMeta.class);
            EventDto dto = new EventDto(annotation.eventType(), method.getName(), clazz);
            result.computeIfAbsent(dto.eventType, eventType -> new ArrayList<>()).add(dto);

        }
        return result;
    }


    @AllArgsConstructor
    @Getter
    public static final class EventDto {
        private EventType eventType;
        private String methodName;
        private Class eventClazz;

        @Override
        public String toString() {
            return "EventDto{" +
                    "eventType=" + eventType +
                    ", methodName='" + methodName + '\'' +
                    ", eventClazz=" + eventClazz.getSimpleName() +
                    '}';
        }
    }
}
