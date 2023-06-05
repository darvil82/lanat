package lanat.exceptions;

import lanat.ArgumentGroup;
import lanat.ArgumentGroupAdder;
import lanat.NamedWithDescription;
import org.jetbrains.annotations.NotNull;

/**
 * Thrown when an {@link ArgumentGroup} is added to a container that already contains an {@link ArgumentGroup} with the
 * same name.
 */
public class ArgumentGroupAlreadyExistsException extends ObjectAlreadyExistsException {
	public <T extends NamedWithDescription & ArgumentGroupAdder>
	ArgumentGroupAlreadyExistsException(@NotNull ArgumentGroup group, final T container) {
		super("Group", group, container);
	}
}
