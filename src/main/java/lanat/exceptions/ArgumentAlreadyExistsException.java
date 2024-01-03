package lanat.exceptions;

import lanat.Argument;
import lanat.utils.ArgumentAdder;
import lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an {@link Argument} is added to a container that already contains an {@link Argument} with the same
 * name.
 */
public class ArgumentAlreadyExistsException extends ObjectAlreadyExistsException {
	public <T extends NamedWithDescription & ArgumentAdder>
	ArgumentAlreadyExistsException(@NotNull Argument<?, ?> argument, @NotNull T container) {
		super("Argument", argument, container);
	}
}