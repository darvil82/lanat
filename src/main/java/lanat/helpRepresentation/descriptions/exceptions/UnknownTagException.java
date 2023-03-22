package lanat.helpRepresentation.descriptions.exceptions;

import lanat.exceptions.LanatException;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;

/** Thrown when a tag with an unknown name is attempted to be used. */
public class UnknownTagException extends LanatException {
	public UnknownTagException(@NotNull String tagName) {
		super("tag " + UtlString.surround(tagName) + " does not exist");
	}
}
