package lanat.exceptions;

import lanat.Argument;
import lanat.ArgumentGroupAdder;
import lanat.NamedWithDescription;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an {@link Argument} is added to a container that
 * already contains an {@link Argument} with the same name.
 * */
public class ArgumentAlreadyExistsException extends ObjectAlreadyExistsException {
	public <T extends NamedWithDescription & ArgumentGroupAdder>
	ArgumentAlreadyExistsException(@NotNull Argument<?, ?> argument, @NotNull T container) {
		super("Argument", argument, container);
	}
}
