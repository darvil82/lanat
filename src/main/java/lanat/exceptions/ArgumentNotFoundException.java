package lanat.exceptions;

import lanat.Argument;

public class ArgumentNotFoundException extends RuntimeException {
	public ArgumentNotFoundException(Argument<?, ?> argument) {
		this(argument.getName());
	}

	public ArgumentNotFoundException(String name) {
		super("Argument not found: " + name);
	}
}
