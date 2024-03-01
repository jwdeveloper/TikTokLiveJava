package io.github.jwdeveloper.tiktok.http.mappers;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.net.http.HttpRequest;

public class HttpRequestJsonMapper implements JsonSerializer<HttpRequest>
{
	@Override
	public JsonElement serialize(HttpRequest src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("method", src.method());
		object.add("timeout", context.serialize(src.timeout().toString()));
		object.addProperty("expectContinue", src.expectContinue());
		object.add("uri", context.serialize(src.uri()));
		object.add("version", context.serialize(src.version().toString()));
		object.add("headers", context.serialize(src.headers().map()));
		return object;
	}
}