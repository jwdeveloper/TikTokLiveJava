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