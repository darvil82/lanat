package lanat.exceptions;

import org.jetbrains.annotations.NotNull;

/** Thrown when an error occurs in an {@link lanat.ArgumentType}. */
public class ArgumentTypeException extends LanatException {
	public ArgumentTypeException(@NotNull String message, @NotNull Throwable cause) {
		super(message, cause);
	}

	public ArgumentTypeException(@NotNull String message) {
		super(message);
	}
}
