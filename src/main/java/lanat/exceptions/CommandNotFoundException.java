package lanat.exceptions;

/** Thrown when a {@link lanat.Command} is not found. */
public class CommandNotFoundException extends LanatException {
	public CommandNotFoundException(String name) {
		super("Command not found: " + name);
	}
}
