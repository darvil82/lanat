package lanat.exceptions;

/**
 * Thrown when an error occurs when inferring types for an {@link lanat.ArgumentType}.
 */
public class ArgumentTypeInferException extends ArgumentTypeException {
	public ArgumentTypeInferException(Class<?> clazz) {
		super("No argument type found for type: " + clazz.getName());
	}
}
