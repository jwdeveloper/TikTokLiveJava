package io.github.jwdeveloper.tiktok.http.mappers;

import com.google.gson.JsonParser;
import io.github.jwdeveloper.tiktok.data.requests.SingServerResponse;

public class SignServerResponseMapper {
    public SingServerResponse map(String json) {
        var parsedJson = JsonParser.parseString(json);
        var jsonObject = parsedJson.getAsJsonObject();

        var signUrl = jsonObject.get("signedUrl").getAsString();
        var userAgent = jsonObject.get("User-Agent").getAsString();
        return new SingServerResponse(signUrl, userAgent);
    }
}
