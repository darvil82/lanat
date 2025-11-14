package io.github.darvil.lanat.exceptions;

import io.github.darvil.lanat.ArgumentType;
import org.jetbrains.annotations.NotNull;

/** Thrown when an error occurs in an {@link ArgumentType}. */
public class ArgumentTypeException extends LanatException {
	public ArgumentTypeException(@NotNull String message, @NotNull Throwable cause) {
		super(message, cause);
	}

	public ArgumentTypeException(@NotNull String message) {
		super(message);
	}
}
