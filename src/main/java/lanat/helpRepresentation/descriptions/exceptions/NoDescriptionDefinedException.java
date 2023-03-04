package lanat.helpRepresentation.descriptions.exceptions;

import lanat.NamedWithDescription;
import lanat.exceptions.LanatException;
import lanat.utils.UtlReflection;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;

public class NoDescriptionDefinedException extends LanatException {
	public NoDescriptionDefinedException(@NotNull NamedWithDescription user) {
		super(
			"No description defined for "
			+ UtlReflection.getSimpleName(user.getClass()) + " " + UtlString.surround(user.getName())
		);
	}
}
