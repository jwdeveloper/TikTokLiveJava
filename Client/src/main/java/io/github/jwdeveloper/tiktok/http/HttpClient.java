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

import java.io.UnsupportedEncodingException;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor
public class HttpClient {

    private static final Pattern CHARSET_PATTERN = Pattern.compile("charset=(.*?)(?=&|$)");

    protected final HttpClientSettings httpClientSettings;
    protected final String url;

    public ActionResult<Response> toResponse() {
        OkHttpClient client = this.prepareClient();
        Request request = this.prepareGetRequest();
        try {
            Response response = client.newCall(request).execute();
            ActionResultBuilder<Response> result = ActionResult.of(response);
			return response.code() != 200 ? result.message("HttpResponse Code: ", response.code()).failure() : result.success();
		} catch (Exception e) {
            throw new TikTokLiveRequestException(e);
        }
    }

    public ActionResult<String> toJsonResponse() {
        return this.toResponse().map(response -> {
            try {
                return response.body().string();
            } catch (Exception ignored) {}
            return "";
        });
    }

    private Charset charsetFrom(Headers headers) {
        String type = headers.get("Content-type") != null ? headers.get("Content-type") : "text/html; charset=utf-8";
        int i = type.indexOf(";");
        if (i >= 0) type = type.substring(i+1);
        try {
            Matcher matcher = CHARSET_PATTERN.matcher(type);
            if (!matcher.find())
                return StandardCharsets.UTF_8;
            return Charset.forName(matcher.group(1));
        } catch (Throwable x) {
            return StandardCharsets.UTF_8;
        }
    }

    public ActionResult<byte[]> toBinaryResponse() {
        return this.toResponse().map(response -> {
            try {
                return response.body().bytes();
            } catch (Exception ignored) {}
            return new byte[0];
        });
    }

    public URI toUrl() {
        String stringUrl = this.prepareUrlWithParameters(this.url, this.httpClientSettings.getParams());
        return URI.create(stringUrl);
    }

    protected Request prepareGetRequest() {
        Request.Builder requestBuilder = new Request.Builder()
                .url(HttpUrl.get(this.toUrl()));
        httpClientSettings.getHeaders().forEach(requestBuilder::addHeader);
        httpClientSettings.getOnRequestCreating().accept(requestBuilder);
        return requestBuilder.build();
    }

    protected OkHttpClient prepareClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
            .followRedirects(true)
            .followSslRedirects(true)
            .cookieJar(new JavaNetCookieJar(new CookieManager()))
            .connectTimeout(httpClientSettings.getTimeout());

        httpClientSettings.getOnClientCreating().accept(builder);
        return builder.build();
    }

    protected static String prepareUrlWithParameters(String url, Map<String, Object> parameters) {
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
