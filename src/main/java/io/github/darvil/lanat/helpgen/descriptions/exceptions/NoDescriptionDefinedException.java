package io.github.darvil.lanat.helpgen.descriptions.exceptions;

import io.github.darvil.lanat.exceptions.LanatException;
import io.github.darvil.lanat.utils.NamedWithDescription;
import io.github.darvil.utils.UtlReflection;
import org.jetbrains.annotations.NotNull;

/** Thrown when a description was not defined for an object. */
public class NoDescriptionDefinedException extends LanatException {
	public NoDescriptionDefinedException(@NotNull NamedWithDescription user) {
		super(
			"No description defined for "
				+ UtlReflection.getSimpleName(user.getClass()) + " '" + user.getName() + "'"
		);
	}
}