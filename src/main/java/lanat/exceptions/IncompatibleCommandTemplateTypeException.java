package lanat.exceptions;

/**
 * Thrown when a type of field inside a {@link lanat.CommandTemplate} is incompatible with the type returned by the
 * argument type inner value.
 */
public class IncompatibleCommandTemplateTypeException extends CommandTemplateException {
	public IncompatibleCommandTemplateTypeException(String message) {
		super(message);
	}
}