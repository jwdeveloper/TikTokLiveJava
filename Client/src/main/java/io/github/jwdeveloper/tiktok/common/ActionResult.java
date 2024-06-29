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
package io.github.jwdeveloper.tiktok.common;

import com.google.gson.*;
import io.github.jwdeveloper.tiktok.http.mappers.*;
import lombok.Data;
import lombok.experimental.Accessors;

import java.net.http.*;
import java.util.Optional;
import java.util.function.Function;

@Data
public class ActionResult<T> {

	private static final Gson gson = new Gson().newBuilder().disableHtmlEscaping()
		.registerTypeHierarchyAdapter(HttpResponse.class, new HttpResponseJsonMapper())
		.registerTypeHierarchyAdapter(HttpRequest.class, new HttpRequestJsonMapper())
		.setPrettyPrinting().create();

	private boolean success = true;
	private String message;
	private T content;
	@Accessors(chain = true, fluent = true)
	private ActionResult<?> previous;

	protected ActionResult(T object) {
		this.content = object;
	}

	protected ActionResult(T object, boolean success) {
		this(object);
		this.success = success;
	}

	protected ActionResult(T object, boolean success, String message) {
		this(object, success);
		this.message = message;
	}

	public static <T> ActionResultBuilder<T> of(T content) {
		return new ActionResultBuilder<>(content);
	}

	public static <T> ActionResult<T> of(Optional<T> optional) {
		return new ActionResult<>(optional.orElse(null), optional.isPresent());
	}

	public boolean isFailure() {
		return !isSuccess();
	}

	public boolean hasMessage() {
		return message != null;
	}

	public boolean hasPrevious() {
		return previous != null;
	}

	public boolean hasContent() {
		return content != null;
	}

	public <Output> ActionResult<Output> cast(Output output) {
		return new ActionResult<>(output, this.isSuccess(), this.getMessage());
	}

	public <Output> ActionResult<Output> cast() {
		return cast(null);
	}

	public <U> ActionResult<U> map(Function<? super T, ? extends U> mapper) {
		return hasContent() ? cast(mapper.apply(content)) : cast();
	}

	public static <T> ActionResult<T> success(T payload, String message) {
		return new ActionResult<>(payload, true, message);
	}

	public static <T> ActionResult<T> success(T payload) {
		return success(payload, null);
	}

	public static <T> ActionResult<T> success() {
		return success(null);
	}

	public static <T> ActionResult<T> failure(T target, String message) {
		return new ActionResult<>(target, false, message);
	}

	public static <T> ActionResult<T> failure(String message) {
		return failure(null, message);
	}

	public static <T> ActionResult<T> failure() {
		return failure(null);
	}

	public JsonObject toJson() {
		JsonObject map = new JsonObject();
		map.addProperty("success", success);
		map.addProperty("message", message);
		map.add("content", gson.toJsonTree(content));
		map.add("previous", hasPrevious() ? previous.toJson() : null);
		return map;
	}

	@Override
	public String toString() {
		return "ActionResult: "+gson.toJson(toJson());
	}
}