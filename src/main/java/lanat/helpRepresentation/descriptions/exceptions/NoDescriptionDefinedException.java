package lanat.helpRepresentation.descriptions.exceptions;

import lanat.NamedWithDescription;
import lanat.exceptions.LanatException;
import lanat.utils.UtlReflection;
import lanat.utils.UtlString;

public class NoDescriptionDefinedException extends LanatException {
	public NoDescriptionDefinedException(NamedWithDescription user) {
		super(
			"No description defined for "
			+ UtlReflection.getSimpleName(user.getClass()) + " " + UtlString.surround(user.getName())
		);
	}
}
