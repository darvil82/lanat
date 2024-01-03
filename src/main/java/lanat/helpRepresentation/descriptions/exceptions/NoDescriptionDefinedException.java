package lanat.helpRepresentation.descriptions.exceptions;

import lanat.exceptions.LanatException;
import lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;
import utils.UtlReflection;

/** Thrown when a description was not defined for an object. */
public class NoDescriptionDefinedException extends LanatException {
	public NoDescriptionDefinedException(@NotNull NamedWithDescription user) {
		super(
			"No description defined for "
				+ UtlReflection.getSimpleName(user.getClass()) + " '" + user.getName() + "'"
		);
	}
}