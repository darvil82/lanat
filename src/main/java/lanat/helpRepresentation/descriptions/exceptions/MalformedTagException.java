package lanat.helpRepresentation.descriptions.exceptions;

import lanat.exceptions.LanatException;

public class MalformedTagException extends LanatException {
	public MalformedTagException(String tagName, String reason) {
		super("tag " + tagName + " is malformed" + (reason == null ? "" : ": " + reason));
	}

	public MalformedTagException(String message) {
		super(message);
	}
}
