package io.github.jwdeveloper.tiktok.http;


import io.github.jwdeveloper.tiktok.Constants;
import lombok.SneakyThrows;

import java.net.CookieManager;
import java.net.ProxySelector;
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

public class TikTokHttpRequestFactory implements TikTokHttpRequest
{
    private CookieManager cookieManager;
    private HttpClient client;

    private Duration timeout;

    private ProxySelector webProxy;
    private String query;
    private Boolean sent;
    private Map<String, String> defaultHeaders;

    public TikTokHttpRequestFactory() {

        cookieManager = new CookieManager();
        defaultHeaders = Constants.DefaultRequestHeaders();
        client = HttpClient.newBuilder()
                .cookieHandler(cookieManager)
                .connectTimeout(Duration.ofSeconds(2))
                .build();
    }

    @SneakyThrows
    public String Get(String url) {
        var uri = URI.create(url);
        var request = HttpRequest.newBuilder().GET();
        for(var header : defaultHeaders.entrySet())
        {
            //request.setHeader(header.getKey(),header.getValue());
        }
        if (query != null) {
            var baseUri = uri.toString();
            var requestUri = URI.create(baseUri + "?" + query);
            request.uri(requestUri);
        }

       return GetContent(request.build());
    }

    @SneakyThrows
    public String Post(String url, HttpRequest.BodyPublisher data) {
        var uri = URI.create(url);
        var request = HttpRequest.newBuilder().POST(data);
        for(var header : defaultHeaders.entrySet())
        {
            request.setHeader(header.getKey(),header.getValue());
        }
        if (query != null) {
            var baseUri = uri.toString();
            var requestUri = URI.create(baseUri + "?" + query);
            request.uri(requestUri);
        }
        return GetContent(request.build());
    }

    public TikTokHttpRequest setHeader(String key, String value)
    {
        defaultHeaders.put(key,value);
        return this;
    }

    public TikTokHttpRequest setAgent( String value)
    {
        defaultHeaders.put("User-Agent", value);
        return this;
    }

    public TikTokHttpRequest SetQueries(Map<String, Object> queries) {
        if (queries == null)
            return this;
        query = String.join("&", queries.entrySet().stream().map(x ->
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


   


    private String GetContent(HttpRequest request) throws Exception {
        var response = client.send(request, HttpResponse.BodyHandlers.ofString());
        sent = true;
        if (response.statusCode()  == 404)
        {
            throw new RuntimeException("Request responded with 404 NOT_FOUND");
        }

        if(response.statusCode() != 200)
        {
            throw new RuntimeException("Request was unsuccessful "+response.statusCode());
        }


        var cookies = response.headers().allValues("Set-Cookie");
        for(var cookie : cookies)
        {
            var split = cookie.split(";")[0].split("=");
            var uri = request.uri();
            var map = new HashMap<String,List<String>>();
            map.put(split[0],List.of(split[1]));
            cookieManager.put(uri,map);

        }
        return response.body();
    }


}
