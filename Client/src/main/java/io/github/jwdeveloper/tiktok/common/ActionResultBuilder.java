package io.github.jwdeveloper.tiktok.common;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ActionResultBuilder<T>
{
	private final T content;
	private String message;

	public ActionResultBuilder(T content) {
		this.content = content;
	}

	public ActionResultBuilder<T> message(Object... messages) {
		this.message = Arrays.stream(messages).map(Object::toString).collect(Collectors.joining(" "));
		return this;
	}

	public ActionResult<T> success() {
		return ActionResult.success(content, message);
	}

	public ActionResult<T> failure() {
		return ActionResult.success(content, message);
	}
}