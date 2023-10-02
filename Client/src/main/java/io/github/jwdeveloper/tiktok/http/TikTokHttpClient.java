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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jwdeveloper.tiktok.Constants;
import io.github.jwdeveloper.tiktok.exceptions.TikTokLiveRequestException;
import io.github.jwdeveloper.tiktok.messages.WebcastResponse;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class TikTokHttpClient {
    private final TikTokHttpRequestFactory requestFactory;
    private final TikTokCookieJar tikTokCookieJar;

    public TikTokHttpClient(TikTokCookieJar tikTokCookieJar, TikTokHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        this.tikTokCookieJar = tikTokCookieJar;
    }

    public void setSessionId(String sessionId)
    {
        tikTokCookieJar.set("sessionid", sessionId);
        tikTokCookieJar.set("sessionid_ss", sessionId);
        tikTokCookieJar.set("sid_tt", sessionId);
    }


    public String getLivestreamPage(String userName) {

        var url = Constants.TIKTOK_URL_WEB + "@" + userName + "/live/";
        var get = getRequest(url, null);
        return get;
    }

    public String postMessageToChat(Map<String,Object> parameters)
    {
        var get = postRequest(Constants.TIKTOK_URL_WEBCAST + "room/chat/", parameters);
        return get;
    }

    public JsonObject getJObjectFromWebcastAPI(String path, Map<String, Object> parameters) {
        var get = getRequest(Constants.TIKTOK_URL_WEBCAST + path, parameters);
        var json = JsonParser.parseString(get);
        var jsonObject = json.getAsJsonObject();
        return jsonObject;
    }

    public WebcastResponse getDeserializedMessage(String path, Map<String, Object> parameters) {
        var bytes = getSignRequest(Constants.TIKTOK_URL_WEBCAST + path, parameters);
        try {
            return WebcastResponse.parseFrom(bytes);
        }
        catch (Exception e)
        {
            throw new TikTokLiveRequestException("Unable to deserialize message: "+path,e);
        }
    }

    private String postRequest(String url, Map<String, Object> parameters) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        System.out.println("RomMID: "+parameters.get("room_id"));
        var request = requestFactory.setQueries(parameters);
        return request.post(url);
    }

    private String getRequest(String url, Map<String, Object> parameters) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }

        var request = requestFactory.setQueries(parameters);
        return request.get(url);
    }
    private byte[] getSignRequest(String url, Map<String, Object> parameters) {
        url = getSignedUrl(url, parameters);
        try {
            var client = HttpClient.newHttpClient();
            var request = HttpRequest.newBuilder()
                    .uri(new URI(url))
                    .build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            var cookies = response.headers().allValues("Set-Cookie");
            for(var cookie : cookies)
            {
                var split = cookie.split(";")[0].split("=");


                var key = split[0];
                var value = split[1];
                tikTokCookieJar.set(key, value);
            }

            return response.body();
        }
        catch (Exception e)
        {
            throw new TikTokLiveRequestException("Unable to send signature");
        }
    }


    private String getSignedUrl(String url, Map<String, Object> parameters) {

        var fullUrl = HttpUtils.parseParameters(url,parameters);
        var singHeaders = new TreeMap<String,Object>();
        singHeaders.put("client", "ttlive-java");
        singHeaders.put("uuc", 1);
        singHeaders.put("url", fullUrl);

        var request = requestFactory.setQueries(singHeaders);
        var content = request.get(Constants.TIKTOK_SIGN_API);


        try {
            var json = JsonParser.parseString(content);
            var jsonObject = json.getAsJsonObject();
            var signedUrl = jsonObject.get("signedUrl").getAsString();
            var userAgent = jsonObject.get("User-Agent").getAsString();

            requestFactory.setAgent(userAgent);
            return signedUrl;
        } catch (Exception e) {
            throw new TikTokLiveRequestException("Insufficient values have been supplied for signing. Likely due to an update. Post an issue on GitHub.", e);
        }
    }

}
