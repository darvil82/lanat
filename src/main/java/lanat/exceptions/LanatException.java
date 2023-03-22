package lanat.exceptions;

/** Thrown when an error occurs in Lanat. All Lanat exceptions extend this class. */
public class LanatException extends RuntimeException {
	public LanatException(String message) {
		super(message);
	}

	public LanatException(String message, Throwable cause) {
		super(message, cause);
	}
}
