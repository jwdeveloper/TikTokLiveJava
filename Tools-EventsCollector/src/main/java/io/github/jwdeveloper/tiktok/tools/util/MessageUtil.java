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
package io.github.jwdeveloper.tiktok.tools.util;

import com.google.protobuf.ByteString;
import io.github.jwdeveloper.tiktok.messages.webcast.WebcastResponse;
import io.github.jwdeveloper.tiktok.utils.ConsoleColors;
import io.github.jwdeveloper.tiktok.utils.JsonUtil;

public class MessageUtil
{
    public static String getContent(WebcastResponse.Message message) {
        try {
            var methodName = message.getMethod();
            var inputClazz = Class.forName("io.github.jwdeveloper.tiktok.messages.webcast." + methodName);
            var parseMethod = inputClazz.getDeclaredMethod("parseFrom", ByteString.class);
            var deserialized = parseMethod.invoke(null, message.getPayload());
            return JsonUtil.messageToJson(deserialized);
        } catch (Exception ex) {

            return ConsoleColors.RED+ "Can not find mapper for "+message.getMethod();
        }
    }

    public static String getContent(String methodName, byte[] bytes) {
        try {

            var inputClazz = Class.forName("io.github.jwdeveloper.tiktok.messages.webcast." + methodName);
            var parseMethod = inputClazz.getDeclaredMethod("parseFrom", byte[].class);
            var deserialized = parseMethod.invoke(null, bytes);
            return JsonUtil.messageToJson(deserialized);
        } catch (Exception ex)
        {
            var sb = new StringBuilder();
            sb.append("Can not find mapper for "+methodName);
            sb.append("\n");
            //String jsonString = JsonFormat.printToString(protobufData);
            return sb.toString();
        }
    }

}
