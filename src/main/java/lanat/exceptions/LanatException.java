package lanat.exceptions;

public class LanatException extends RuntimeException {
	public LanatException(String message) {
		super(message);
	}

	public LanatException(String message, Throwable cause) {
		super(message, cause);
	}
}
