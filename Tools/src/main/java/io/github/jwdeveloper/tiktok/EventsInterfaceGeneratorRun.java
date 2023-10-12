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
