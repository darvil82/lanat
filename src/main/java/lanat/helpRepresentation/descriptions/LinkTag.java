package lanat.helpRepresentation.descriptions;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.Command;
import lanat.NamedWithDescription;
import lanat.helpRepresentation.ArgumentGroupRepr;
import lanat.helpRepresentation.ArgumentRepr;
import lanat.helpRepresentation.CommandRepr;
import lanat.helpRepresentation.descriptions.exceptions.InvalidRouteException;
import org.jetbrains.annotations.NotNull;

public class LinkTag extends Tag {
	@Override
	protected @NotNull String parse(@NotNull String value, @NotNull NamedWithDescription user) {
		final var obj = RouteParser.parse(user, value);

		// replace with switch expression when it's out of preview
		if (obj instanceof Command cmd) {
			return CommandRepr.getRepresentation(cmd);
		} else if (obj instanceof Argument<?,?> arg) {
			return ArgumentRepr.getRepresentation(arg);
		} else if (obj instanceof ArgumentGroup group) {
			return ArgumentGroupRepr.getRepresentation(group);
		} else {
			throw new InvalidRouteException(user, value);
		}
	}
}
