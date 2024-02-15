package io.github.jwdeveloper.tiktok.common;

import lombok.Data;

import java.util.function.Function;

@Data
public class ActionResult<T>
{
	private boolean success = true;
	private T content;
	private String message;

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

	public boolean isFailure() {
		return !isSuccess();
	}

	public boolean hasMessage() {
		return message != null;
	}

	public boolean hasContent() {
		return content != null;
	}

	public static <T> ActionResult<T> success() {
		return new ActionResult<>(null, true);
	}

	public <Output> ActionResult<Output> cast(Output output) {
		return new ActionResult<>(output, this.isSuccess(), this.getMessage());
	}

	public <Output> ActionResult<Output> cast() {
		return new ActionResult<>(null, this.isSuccess(), this.getMessage());
	}

	public <U> ActionResult<U> map(Function<? super T, ? extends U> mapper) {
		return hasContent() ? cast(mapper.apply(content)) : cast();
	}

	public static <Input, Output> ActionResult<Output> cast(ActionResult<Input> action, Output output) {
		return new ActionResult<>(output, action.isSuccess(), action.getMessage());
	}

	public static <T> ActionResult<T> success(T payload) {
		return new ActionResult<>(payload, true);
	}

	public static <T> ActionResult<T> success(T payload, String message) {
		return new ActionResult<>(payload, true, message);
	}

	public static <T> ActionResult<T> failure() {
		return new ActionResult<>(null, false);
	}

	public static <T> ActionResult<T> failure(String message) {
		return new ActionResult<>(null, false, message);
	}

	public static <T> ActionResult<T> failure(T target, String message) {
		return new ActionResult<>(target, false, message);
	}
}