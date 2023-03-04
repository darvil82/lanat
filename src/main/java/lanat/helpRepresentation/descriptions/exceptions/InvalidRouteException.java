package lanat.helpRepresentation.descriptions.exceptions;

import lanat.NamedWithDescription;
import lanat.exceptions.LanatException;
import lanat.utils.UtlReflection;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class InvalidRouteException extends LanatException {
	public InvalidRouteException(@NotNull NamedWithDescription user, @NotNull String value) {
		this(user, value, null);
	}

	public InvalidRouteException(@NotNull NamedWithDescription user, @NotNull String value, @Nullable String message) {
		super(
			"invalid route value " + UtlString.surround(value)
				+ " for " + UtlReflection.getSimpleName(user.getClass()) + " " + UtlString.surround(user.getName())
				+ (message == null ? "" : ": " + message)
		);
	}

	public InvalidRouteException(@NotNull String message) {
		super(message);
	}
}
