package lanat.exceptions;

public class ArgumentTypeException extends LanatException {
	public ArgumentTypeException(String message, Throwable cause) {
		super(message, cause);
	}
	public ArgumentTypeException(String message) {
		super(message);
	}
}
