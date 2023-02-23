package lanat.exceptions;

import lanat.Command;

public class CommandAlreadyExistsException extends ObjectAlreadyExistsException {
	public CommandAlreadyExistsException(Command command, Command container) {
		super(command, container);
	}
}
