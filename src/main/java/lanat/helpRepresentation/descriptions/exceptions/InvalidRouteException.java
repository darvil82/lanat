package lanat.helpRepresentation.descriptions.exceptions;

import lanat.NamedWithDescription;
import lanat.exceptions.LanatException;
import lanat.utils.UtlReflection;
import lanat.utils.UtlString;

public class InvalidRouteException extends LanatException {
	public InvalidRouteException(String value, NamedWithDescription user) {
		super(
			"invalid route value " + UtlString.surround(value)
			+ " for " + UtlReflection.getSimpleName(user.getClass()) + " " + UtlString.surround(user.getName())
		);
	}
}
