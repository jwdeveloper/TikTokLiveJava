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

import com.google.protobuf.ByteString;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.mockClient.TikTokClientMock;
import io.github.jwdeveloper.tiktok.tools.collector.db.TikTokDatabase;
import io.github.jwdeveloper.tiktok.tools.collector.tables.TikTokResponseModel;
import io.github.jwdeveloper.tiktok.utils.ConsoleColors;
import io.github.jwdeveloper.tiktok.utils.JsonUtil;

import java.util.logging.ConsoleHandler;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


public class RunDbTester {


    public static void main(String[] args) throws Exception {
        var db = new TikTokDatabase("test");
        db.init();

        var responses = db.selectResponces().stream().map(TikTokResponseModel::getResponse).toList();
        var client = TikTokClientMock
                .create()
                .addResponses(responses)
                .onWebsocketUnhandledMessage((liveClient, event) ->
                {
                    liveClient.getLogger().info("Unhandled Message! " + event.getData().getMethod());
                })
                .onWebsocketMessage((liveClient, event) ->
                {
                    var sb = new StringBuilder();
                    sb.append(event.getEvent().getClass().getSimpleName());
                    sb.append(event.getEvent().toJson());
                   // sb.append(ConsoleColors.YELLOW + messageContent(event.getMessage()));

                    liveClient.getLogger().info(sb.toString());
                })
                .build();

        updateLogger(client.getLogger());
        client.connect();
    }

    protected static String messageContent(WebcastResponse.Message message) {
        try {

            var methodName = message.getMethod();
            if (!methodName.contains("Webcast")) {
                methodName = "Webcast" + methodName;
            }
            var inputClazz = Class.forName("io.github.jwdeveloper.tiktok.messages.webcast." + methodName);
            var parseMethod = inputClazz.getDeclaredMethod("parseFrom", ByteString.class);
            var deserialized = parseMethod.invoke(null, message.getPayload());
            return JsonUtil.messageToJson(deserialized);
        } catch (Exception ex) {
            return "";
        }
    }

    public static void updateLogger(Logger logger) {
        for (var handler : logger.getHandlers()) {
            logger.removeHandler(handler);
        }

        var handler = new ConsoleHandler();
        handler.setFormatter(new Formatter() {
            @Override
            public String format(LogRecord record) {
                return ConsoleColors.WHITE_BRIGHT + record.getLevel() + ": " + ConsoleColors.GREEN + record.getMessage() + "\n" + ConsoleColors.RESET;
            }
        });
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);
    }


}
