package lanat.helpRepresentation.descriptions;

import lanat.*;
import lanat.helpRepresentation.ArgumentGroupRepr;
import lanat.helpRepresentation.ArgumentRepr;
import lanat.helpRepresentation.CommandRepr;
import lanat.helpRepresentation.descriptions.exceptions.InvalidRouteException;
import org.jetbrains.annotations.NotNull;

public class LinkTag extends Tag {
	@Override
	protected @NotNull <T extends CommandUser & NamedWithDescription>
	String parse(@NotNull String value, @NotNull T user) {
		final var obj = RouteParser.parseRoute(user, value);

		if (obj instanceof Command cmd) {
			return CommandRepr.getCommandRepresentation(cmd);
		} else if (obj instanceof Argument<?,?> arg) {
			return ArgumentRepr.getSynopsisRepresentation(arg);
		} else if (obj instanceof ArgumentGroup group) {
			return ArgumentGroupRepr.getRepresentation(group);
		} else {
			throw new InvalidRouteException(value, user);
		}
	}
}
