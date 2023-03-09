package lanat.helpRepresentation.descriptions.tags;

import lanat.NamedWithDescription;
import lanat.helpRepresentation.descriptions.DescriptionFormatter;
import lanat.helpRepresentation.descriptions.RouteParser;
import lanat.helpRepresentation.descriptions.Tag;
import lanat.helpRepresentation.descriptions.exceptions.InvalidRouteException;
import lanat.helpRepresentation.descriptions.exceptions.NoDescriptionDefinedException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Gets the description of the target object specified by the route.
 * @see RouteParser
 * */
public class DescTag extends Tag {
	@Override
	protected @NotNull String parse(@NotNull NamedWithDescription user, @Nullable String value) {
		final var target = RouteParser.parse(user, value);
		if (target == user)
			throw new InvalidRouteException("Cannot use desc tag to describe itself");

		final var description = target.getDescription();
		if (description == null)
			throw new NoDescriptionDefinedException(user);

		return DescriptionFormatter.parse(target, description);
	}
}
