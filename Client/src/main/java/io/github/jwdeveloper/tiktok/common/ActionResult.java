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
	private T content;
	private String message;
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
		map.add("content", gson.toJsonTree(content));
		map.addProperty("message", message);
		map.add("previous", hasPrevious() ? previous.toJson() : null);
		return map;
	}

	@Override
	public String toString() {
		return "ActionResult: "+gson.toJson(toJson());
	}
}