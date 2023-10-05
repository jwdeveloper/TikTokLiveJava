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
package io.github.jwdeveloper.tiktok.tools.tester;

import com.google.gson.JsonParser;
import com.google.protobuf.InvalidProtocolBufferException;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastLinkMicFanTicketMethod;
import io.github.jwdeveloper.tiktok.mockClient.TikTokClientMock;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class RunLogTester {


    public static void main(String[] args) throws IOException {


        var input ="CjwKHVdlYmNhc3RMaW5rTWljRmFuVGlja2V0TWV0aG9kEIWWp7ig2O6OZRiGlviyrNjpjmUgza2G4a8xMAESZwoNCIWIouKYhLqBYhCbARCbASpTaHR0cHM6Ly9wMTYtd2ViY2FzdC50aWt0b2tjZG4uY29tL2ltZy93ZWJjYXN0LXNnL3Rpa3Rva19saW5rbWljX2NvaW5AM3gucG5nfjB4MC5wbmc=";
        var bytes = Base64.getDecoder().decode(input);
        var a= WebcastLinkMicFanTicketMethod.parseFrom(bytes);



        var messages = getMessages();
        var client = TikTokClientMock.create().build();
        for(var msg : messages.entrySet())
        {
            for(var content : msg.getValue())
            {
                client.publishMessage(msg.getKey(),content);
            }
        }
        client.connect();
    }


    private static Map<String, List<String>> getMessages() throws IOException {
        var path = "C:\\Users\\ja\\IdeaProjects\\TikTokLiveJava\\Tools-EventsCollector\\src\\main\\resources\\log.json";
        var jsonElement = JsonParser.parseReader(new FileReader(path, Charset.defaultCharset()));

        var res = new HashMap<String, List<String>>();
        if (jsonElement.isJsonObject()) {
            var jsonObject = jsonElement.getAsJsonObject();
            var keys = jsonObject.keySet();
            for (String key : keys) {
                var messages = jsonObject.get(key).getAsJsonArray();
                for (var msg : messages) {
                    var data = msg.getAsJsonObject().get("eventData").getAsString();
                //    System.out.println("KEY: " + key + "  DATA: " + data);
                    res.computeIfAbsent(key, s -> new ArrayList<>()).add(data);
                }
            }
        }
        return res;
    }
}
