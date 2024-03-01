package io.github.jwdeveloper.tiktok.common;

import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ActionResultBuilder<T>
{
	private final T content;
	private String message;
	@Setter @Accessors(fluent = true, chain = true)
	private ActionResult<?> previous;

	public ActionResultBuilder(T content) {
		this.content = content;
	}

	public ActionResultBuilder<T> message(Object... messages) {
		this.message = Arrays.stream(messages).map(Object::toString).collect(Collectors.joining(" "));
		return this;
	}

	public ActionResult<T> success() {
		return ActionResult.success(content, message).previous(previous);
	}

	public ActionResult<T> failure() {
		return ActionResult.success(content, message).previous(previous);
	}
}