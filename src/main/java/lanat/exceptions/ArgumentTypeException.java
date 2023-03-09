package lanat.exceptions;

/** Thrown when an error occurs in an {@link lanat.ArgumentType}. */
public class ArgumentTypeException extends LanatException {
	public ArgumentTypeException(String message, Throwable cause) {
		super(message, cause);
	}
	public ArgumentTypeException(String message) {
		super(message);
	}
}
