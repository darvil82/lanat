package io.github.darvil.lanat.exceptions;

import io.github.darvil.lanat.ArgumentType;

/**
 * Thrown when an error occurs when inferring types for an {@link ArgumentType}.
 */
public class ArgumentTypeInferException extends ArgumentTypeException {
	public ArgumentTypeInferException(Class<?> clazz) {
		super("No argument type found for type: " + clazz.getName());
	}
}
