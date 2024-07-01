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
import io.github.jwdeveloper.tiktok.data.settings.HttpClientSettings;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import lombok.AllArgsConstructor;

import java.net.*;
import java.net.http.*;
import java.nio.charset.*;
import java.time.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.Collectors;

@AllArgsConstructor
public class HttpClient {

    protected final HttpClientSettings httpClientSettings;
    protected final String url;
    private final Pattern pattern = Pattern.compile("charset=(.*?)(?=&|$)");

    public ActionResult<HttpResponse<byte[]>> toResponse() {
        var client = prepareClient();
        var request = prepareGetRequest();
        try {
            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            var result = ActionResult.of(response);
            return switch (response.statusCode()) {
                case 420 -> result.message("HttpResponse Code:", response.statusCode(), "| IP Cloudflare Blocked.").failure();
                case 429 -> {
                    var wait = response.headers().firstValue("ratelimit-reset");
					if (wait.isEmpty())
                        yield result.message("HttpResponse Code:", response.statusCode(), "| Sign server rate limit reached. Try again later.").failure();
                    Duration duration = Duration.ofSeconds(Long.parseLong(wait.get()));
                    yield result.message("HttpResponse Code:", response.statusCode(),
                        String.format("| Sign server rate limit reached. Try again in %02d:%02d.", duration.toMinutesPart(), duration.toSecondsPart())).failure();
				}
                case 500, 501, 502, 503 -> result.message("HttpResponse Code:", response.statusCode(), "| Sign server Error. Try again later.").failure();
                case 504 -> result.message("HttpResponse Code:", response.statusCode(), "| Sign server Timeout. Try again later.").failure();
                case 200 -> result.success();
                default -> result.message("HttpResponse Code:", response.statusCode()).failure();
            };
		} catch (Exception e) {
            throw new TikTokLiveRequestException(e);
        }
    }

    public ActionResult<String> toJsonResponse() {
        return toResponse().map(content -> new String(content.body(), charsetFrom(content.headers())));
    }

    private Charset charsetFrom(HttpHeaders headers) {
        String type = headers.firstValue("Content-type").orElse("text/html; charset=utf-8");
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
        return toResponse().map(HttpResponse::body);
    }

    public URI toUri() {
        var stringUrl = prepareUrlWithParameters(url, httpClientSettings.getParams());
        return URI.create(stringUrl);
    }

    protected HttpRequest prepareGetRequest() {
        var requestBuilder = HttpRequest.newBuilder().GET();
        requestBuilder.uri(toUri());
        requestBuilder.timeout(httpClientSettings.getTimeout());
        httpClientSettings.getHeaders().forEach(requestBuilder::setHeader);

        httpClientSettings.getOnRequestCreating().accept(requestBuilder);
        return requestBuilder.build();
    }

    protected java.net.http.HttpClient prepareClient() {
        var builder = java.net.http.HttpClient.newBuilder()
            .followRedirects(java.net.http.HttpClient.Redirect.NORMAL)
            .cookieHandler(new CookieManager())
            .connectTimeout(httpClientSettings.getTimeout());

        httpClientSettings.getOnClientCreating().accept(builder);
        return builder.build();
    }

    protected String prepareUrlWithParameters(String url, Map<String, Object> parameters) {
        if (parameters.isEmpty()) {
            return url;
        }

        return url + "?" + parameters.entrySet().stream().map(entry ->
        {
            var encodedKey = URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8);
            var encodedValue = URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8);
            return encodedKey + "=" + encodedValue;
        }).collect(Collectors.joining("&"));
    }
}