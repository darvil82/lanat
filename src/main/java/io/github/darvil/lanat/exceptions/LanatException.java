package io.github.darvil.lanat.exceptions;

import org.jetbrains.annotations.NotNull;

/** Thrown when an error occurs in Lanat. All Lanat exceptions extend this class. */
public class LanatException extends RuntimeException {
	public LanatException(@NotNull String message) {
		super(message);
	}

	public LanatException(@NotNull String message, @NotNull Throwable cause) {
		super(message, cause);
	}
}
