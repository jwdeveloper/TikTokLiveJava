package io.github.jwdeveloper.tiktok.http;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.jwdeveloper.tiktok.ClientSettings;
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

public class TikTokHttpApiClient {
    private final TikTokHttpRequestFactory requestFactory;
    private final TikTokCookieJar tikTokCookieJar;

    public TikTokHttpApiClient(TikTokCookieJar tikTokCookieJar,  TikTokHttpRequestFactory requestFactory) {
        this.requestFactory = requestFactory;
        this.tikTokCookieJar = tikTokCookieJar;
    }

    public String GetLivestreamPage(String userName) {

        var url = Constants.TIKTOK_URL_WEB + "@" + userName + "/live/";
        var get = getRequest(url, null);
        return get;
    }

    public JsonObject GetJObjectFromWebcastAPI(String path, Map<String, Object> parameters) {
        var get = getRequest(Constants.TIKTOK_URL_WEBCAST + path, parameters);
        var json = JsonParser.parseString(get);
        var jsonObject = json.getAsJsonObject();
        return jsonObject;
    }

    public WebcastResponse GetDeserializedMessage(String path, Map<String, Object> parameters) {
        var bytes = getSignRequest(Constants.TIKTOK_URL_WEBCAST + path, parameters);
        try {
            return WebcastResponse.parseFrom(bytes);
        }
        catch (Exception e)
        {
            throw new TikTokLiveRequestException("Unable to deserialize message: "+path,e);
        }
    }

    private String getRequest(String url, Map<String, Object> parameters) {
        if (parameters == null) {
            parameters = new HashMap<>();
        }

        var request = requestFactory.SetQueries(parameters);
        return request.Get(url);
    }
    private byte[] getSignRequest(String url, Map<String, Object> parameters) {
        url = GetSignedUrl(url, parameters);
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


    private String GetSignedUrl(String url, Map<String, Object> parameters) {

        var fullUrl = HttpUtils.parseParameters(url,parameters);
        var singHeaders = new TreeMap<String,Object>();
        singHeaders.put("client", "ttlive-java");
        singHeaders.put("uuc", 1);
        singHeaders.put("url", fullUrl);

        var request = requestFactory.SetQueries(singHeaders);
        var content = request.Get(Constants.TIKTOK_SIGN_API);


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
