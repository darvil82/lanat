package lanat.exceptions;

import lanat.Command;

/** Thrown when a {@link Command} is added to a container that already contains a {@link Command} with the same name. */
public class CommandAlreadyExistsException extends ObjectAlreadyExistsException {
	public CommandAlreadyExistsException(Command command, Command container) {
		super(command, container);
	}
}
