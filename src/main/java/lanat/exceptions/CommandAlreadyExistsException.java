package lanat.exceptions;

import lanat.Command;
import org.jetbrains.annotations.NotNull;

/** Thrown when a {@link Command} is added to a container that already contains a {@link Command} with the same name. */
public class CommandAlreadyExistsException extends ObjectAlreadyExistsException {
	public CommandAlreadyExistsException(@NotNull Command command, @NotNull Command container) {
		super("Command", command, container);
	}
}
