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

import io.github.jwdeveloper.tiktok.common.ActionResult;
import io.github.jwdeveloper.tiktok.common.ActionResultBuilder;
import io.github.jwdeveloper.tiktok.data.settings.HttpClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import lombok.AllArgsConstructor;
import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class HttpClient {

    protected final HttpClientSettings httpClientSettings;
    protected final String url;
    private final Pattern pattern = Pattern.compile("charset=(.*?)(?=&|$)");

    public ActionResult<Response> toResponse() {
        OkHttpClient client = prepareClient();
        Request request = prepareGetRequest();
        try {
            Response response = client.newCall(request).execute();
            ActionResultBuilder<Response> result = ActionResult.of(response);
			return response.code() != 200 ? result.message("HttpResponse Code: ", response.code()).failure() : result.success();
		} catch (Exception e) {
            throw new TikTokLiveRequestException(e);
        }
    }

    public ActionResult<String> toJsonResponse() {
        return toResponse().map(content -> {
            try {
                return new String(content.body() != null ? content.body().bytes() : new byte[0], charsetFrom(content.headers()));
            } catch (IOException ignored) {
                return "";
            }
        });
    }

    private Charset charsetFrom(Headers headers) {
        String type = headers.get("Content-type") != null ? headers.get("Content-type") : "text/html; charset=utf-8";
        int i = type.indexOf(";");
        if (i >= 0) type = type.substring(i+1);
        try {
            Matcher matcher = pattern.matcher(type);
            if (!matcher.find())
                return StandardCharsets.UTF_8;
            return Charset.forName(matcher.group(1));
        } catch (Throwable x) {
            return StandardCharsets.UTF_8;
        }
    }

    public ActionResult<byte[]> toBinaryResponse() {
        return toResponse().map(response -> {
            if (response.body() != null)
                try {
                    return response.body().bytes();
                } catch (IOException ignored) {}
            return new byte[0];
        });
    }

    public URI toUrl() {
        String stringUrl = prepareUrlWithParameters(url, httpClientSettings.getParams());
        return URI.create(stringUrl);
    }

    protected Request prepareGetRequest() {
        Request.Builder requestBuilder = new Request.Builder();
        requestBuilder.url(url);
        httpClientSettings.getHeaders().forEach(requestBuilder::addHeader);

        httpClientSettings.getOnRequestCreating().accept(requestBuilder);
        return requestBuilder.build();
    }

    protected OkHttpClient prepareClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .followRedirects(true)
            .cookieJar(CookieJar.NO_COOKIES)
            .connectTimeout(httpClientSettings.getTimeout());

        httpClientSettings.getOnClientCreating().accept(builder);
        return builder.build();
    }

    protected String prepareUrlWithParameters(String url, Map<String, Object> parameters) {
        if (parameters.isEmpty()) {
            return url;
        }

        return url + "?" + parameters.entrySet().stream().map(entry -> {
            try {
                String encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8.name());
                String encodedValue = URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8.name());
                return encodedKey + "=" + encodedValue;
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.joining("&"));
    }
}
