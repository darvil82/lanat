package lanat.helpRepresentation.descriptions.tags;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.Command;
import lanat.helpRepresentation.ArgumentGroupRepr;
import lanat.helpRepresentation.ArgumentRepr;
import lanat.helpRepresentation.CommandRepr;
import lanat.helpRepresentation.descriptions.RouteParser;
import lanat.helpRepresentation.descriptions.Tag;
import lanat.helpRepresentation.descriptions.exceptions.InvalidRouteException;
import lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Gets the representation of the target object specified by the route.
 *
 * @see RouteParser
 */
public class LinkTag extends Tag {
	@Override
	protected @NotNull String parse(@NotNull NamedWithDescription user, @Nullable String value) {
		final var obj = RouteParser.parse(user, value);

		// replace with switch expression when it's out of preview
		if (obj instanceof Command cmd)
			return CommandRepr.getRepresentation(cmd);
		else if (obj instanceof Argument<?, ?> arg)
			return ArgumentRepr.getRepresentation(arg, false);
		else if (obj instanceof ArgumentGroup group)
			return ArgumentGroupRepr.getRepresentation(group);

		throw new InvalidRouteException(user, value, "The route must point to a command, argument or argument group.");
	}
}