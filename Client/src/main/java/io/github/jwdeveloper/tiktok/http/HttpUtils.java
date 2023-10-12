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
package io.github.jwdeveloper.tiktok.http;

import lombok.SneakyThrows;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class HttpUtils
{
    public static String parseParameters(String url, Map<String,Object> parameters)
    {
        var parameterString = "";
        if (!parameters.isEmpty()) {
            var builder = new StringBuilder();
            builder.append("?");
            var first = false;
            for (var param : parameters.entrySet()) {

                if (first) {
                    builder.append("&");
                }
                builder.append(param.getKey()).append("=").append(param.getValue());
                first = true;
            }
            parameterString = builder.toString();
        }

        return url+parameterString;
    }

    @SneakyThrows
    public static String parseParametersEncode(String url, Map<String,Object> parameters)
    {

        var parameterString = "";
        if (!parameters.isEmpty()) {
            var builder = new StringBuilder();
            builder.append("?");
            var first = false;
            for (var param : parameters.entrySet()) {

                if (first) {
                    builder.append("&");
                }

                final String encodedKey = URLEncoder.encode(param.getKey(), StandardCharsets.UTF_8);
                final String encodedValue = URLEncoder.encode(param.getValue().toString(), StandardCharsets.UTF_8);
                builder.append(encodedKey).append("=").append(encodedValue);
                first = true;
            }
            parameterString = builder.toString();
        }

        return url+parameterString;
    }
}
