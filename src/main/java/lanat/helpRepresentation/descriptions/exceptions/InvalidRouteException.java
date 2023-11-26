package lanat.helpRepresentation.descriptions.exceptions;

import lanat.NamedWithDescription;
import lanat.exceptions.LanatException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import utils.UtlReflection;

/** Thrown when a parsed route is invalid. */
public class InvalidRouteException extends LanatException {
	public InvalidRouteException(@NotNull NamedWithDescription user, @Nullable String value) {
		this(user, value, null);
	}

	public InvalidRouteException(@NotNull NamedWithDescription user, @Nullable String value, @Nullable String message) {
		super(
			"invalid route value '" + value + "' for "
				+ UtlReflection.getSimpleName(user.getClass()) + " '" + user.getName() + "'"
				+ (message == null ? "" : ": " + message)
		);
	}

	public InvalidRouteException(@NotNull String message) {
		super(message);
	}
}
