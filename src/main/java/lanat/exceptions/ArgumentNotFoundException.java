package lanat.exceptions;

import lanat.Argument;

/** Thrown when an {@link Argument} is not found. */
public class ArgumentNotFoundException extends LanatException {
	public ArgumentNotFoundException(Argument<?, ?> argument) {
		this(argument.getName());
	}

	public ArgumentNotFoundException(String name) {
		super("Argument not found: " + name);
	}
}
