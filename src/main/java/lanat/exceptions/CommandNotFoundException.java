package lanat.exceptions;

public class CommandNotFoundException extends LanatException {
	public CommandNotFoundException(String name) {
		super("Command not found: " + name);
	}
}
