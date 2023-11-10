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


import io.github.jwdeveloper.tiktok.Constants;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import lombok.SneakyThrows;

import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class TikTokHttpRequestFactory implements TikTokHttpRequest {
    private final CookieManager cookieManager;
    private final Map<String, String> defaultHeaders;
    private final TikTokCookieJar tikTokCookieJar;
    private final HttpClient client;
    private String query;

    public TikTokHttpRequestFactory(TikTokCookieJar tikTokCookieJar) {
        this.tikTokCookieJar = tikTokCookieJar;
        this.cookieManager = new CookieManager();
        defaultHeaders = Constants.DefaultRequestHeaders();
        client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }
    @SneakyThrows
    public String get(String url) {
        var uri = URI.create(url);
        var requestBuilder = HttpRequest.newBuilder().GET();

        for (var header : defaultHeaders.entrySet())
        {
            if(header.getKey().equals("Connection") || header.getKey().equals("Accept-Encoding"))
            {
                continue;
            }
            requestBuilder.setHeader(header.getKey(), header.getValue());
        }
        if (query != null) {
            var baseUri = uri.toString();
            var requestUri = URI.create(baseUri + "?" + query);
            requestBuilder.uri(requestUri);
        }
        else
        {
            requestBuilder.uri(uri);
        }

        var result = requestBuilder.build();

        return getContent(result);
    }

    @SneakyThrows
    public String post(String url) {
        var uri = URI.create(url);
        var request = HttpRequest.newBuilder().POST(HttpRequest.BodyPublishers.ofString(""));
        for (var header : defaultHeaders.entrySet())
        {
            if(header.getKey().equals("Connection"))
            {
                continue;
            }
            request.setHeader(header.getKey(), header.getValue());
        }
        request.setHeader("Content-type","application/x-www-form-urlencoded; charset=UTF-8");
        request.setHeader("Cookie", tikTokCookieJar.parseCookies());


        if (query != null) {
            var baseUri = uri.toString();
            var requestUri = URI.create(baseUri + "?" + query);
            request.uri(requestUri);
            System.out.println(requestUri.toString());
        }



        return getContent(request.build());
    }

    public TikTokHttpRequest setHeader(String key, String value) {
        defaultHeaders.put(key, value);
        return this;
    }

    public TikTokHttpRequest setAgent(String value) {
        defaultHeaders.put("User-Agent", value);
        return this;
    }

    public TikTokHttpRequest setQueries(Map<String, Object> queries) {
        if (queries == null)
            return this;
        var testMap = new TreeMap<String,Object>(queries);
        query = String.join("&", testMap.entrySet().stream().map(x ->
        {
            var key = x.getKey();
            var value = "";
            try {
                value = URLEncoder.encode(x.getValue().toString(), StandardCharsets.UTF_8);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return key + "=" + value;
        }).toList());
        return this;
    }


    private String getContent(HttpRequest request) throws Exception {
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 404) {
            throw new TikTokLiveRequestException("Request responded with 404 NOT_FOUND");
        }

        if (response.statusCode() != 200) {
            throw new TikTokLiveRequestException("Request was unsuccessful " + response.statusCode());
        }

        var cookies = response.headers().allValues("Set-Cookie");
        for (var cookie : cookies) {
            var split = cookie.split(";")[0].split("=");
            var uri = request.uri();


            var key = split[0];
            var value = split[1];
            tikTokCookieJar.set(key, value);

            var map = new HashMap<String, List<String>>();
            map.put(key, List.of(value));
            cookieManager.put(uri, map);

        }
        return response.body();
    }


}
