package io.github.darvil.lanat.helpRepresentation.descriptions.exceptions;

import io.github.darvil.lanat.exceptions.LanatException;
import io.github.darvil.lanat.utils.NamedWithDescription;
import io.github.darvil.utils.UtlReflection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/** Thrown when a parsed route is invalid. */
public class InvalidRouteException extends LanatException {
	public InvalidRouteException(@NotNull NamedWithDescription user, @Nullable String value) {
		this(user, value, null);
	}

	public InvalidRouteException(@NotNull NamedWithDescription user, @Nullable String value, @Nullable String message) {
		super(
			"Invalid route value '" + value + "' for "
				+ UtlReflection.getSimpleName(user.getClass()) + " '" + user.getName() + "'"
				+ (message == null ? "" : ": " + message)
		);
	}

	public InvalidRouteException(@NotNull String message) {
		super(message);
	}
}