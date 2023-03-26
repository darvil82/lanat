package lanat.exceptions;

import lanat.Argument;
import lanat.NamedWithDescription;
import org.jetbrains.annotations.NotNull;

/** Thrown when an {@link Argument} is not found. */
public class ArgumentNotFoundException extends ObjectNotFoundException {
	public ArgumentNotFoundException(@NotNull Argument<?, ?> argument) {
		super("Argument", argument);
	}

	public ArgumentNotFoundException(@NotNull String name) {
		super("Argument", name);
	}

	public ArgumentNotFoundException(@NotNull Argument<?, ?> argument, @NotNull NamedWithDescription container) {
		super("Argument", argument, container);
	}
}
