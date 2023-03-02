package lanat.helpRepresentation.descriptions.exceptions;

import lanat.exceptions.LanatException;
import lanat.utils.UtlString;

public class UnknownTagException extends LanatException {
	public UnknownTagException(String tagName) {
		super("tag " + UtlString.surround(tagName) + " does not exist");
	}
}
