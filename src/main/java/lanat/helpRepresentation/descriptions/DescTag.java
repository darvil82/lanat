package lanat.helpRepresentation.descriptions;

import lanat.CommandUser;
import lanat.NamedWithDescription;
import lanat.helpRepresentation.descriptions.exceptions.InvalidRouteException;
import lanat.helpRepresentation.descriptions.exceptions.NoDescriptionDefinedException;
import org.jetbrains.annotations.NotNull;

public class DescTag extends Tag {
	@Override
	protected @NotNull <T extends CommandUser & NamedWithDescription>
	String parse(@NotNull String value, @NotNull T user) {
		final var target = RouteParser.parse(user, value);
		if (target == user)
			throw new InvalidRouteException("Cannot use desc tag to describe itself");

		final var description = target.getDescription();
		if (description == null)
			throw new NoDescriptionDefinedException(user);

		return DescriptionFormatter.parse(user, description);
	}
}
