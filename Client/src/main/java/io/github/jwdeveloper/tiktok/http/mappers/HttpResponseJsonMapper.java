package io.github.jwdeveloper.tiktok.http.mappers;

import com.google.gson.*;

import java.lang.reflect.Type;
import java.net.http.HttpResponse;

public class HttpResponseJsonMapper implements JsonSerializer<HttpResponse>
{
	@Override
	public JsonElement serialize(HttpResponse src, Type typeOfSrc, JsonSerializationContext context) {
		JsonObject object = new JsonObject();
		object.addProperty("statusCode", src.statusCode());
		object.add("request", context.serialize(src.request()));
		object.add("headers", context.serialize(src.headers().map()));
		object.add("body", context.serialize(src.body()));
		object.add("uri", context.serialize(src.uri().toString()));
		object.add("version", context.serialize(src.version().toString()));
		return object;
	}
}