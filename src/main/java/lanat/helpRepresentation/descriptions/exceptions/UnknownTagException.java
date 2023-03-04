package lanat.helpRepresentation.descriptions.exceptions;

import lanat.exceptions.LanatException;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;

public class UnknownTagException extends LanatException {
	public UnknownTagException(@NotNull String tagName) {
		super("tag " + UtlString.surround(tagName) + " does not exist");
	}
}
