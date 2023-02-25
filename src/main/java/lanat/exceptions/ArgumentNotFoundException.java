package lanat.exceptions;

import lanat.Argument;

public class ArgumentNotFoundException extends LanatException {
	public ArgumentNotFoundException(Argument<?, ?> argument) {
		this(argument.getName());
	}

	public ArgumentNotFoundException(String name) {
		super("Argument not found: " + name);
	}
}
