package lanat.helpRepresentation.descriptions;

import lanat.CommandUser;
import lanat.NamedWithDescription;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;

public class LinkTag extends Tag {
	@Override
	protected @NotNull <T extends CommandUser & NamedWithDescription> String parse(@NotNull String value, @NotNull T user) {
		return UtlString.fromNullable(RouteParser.parseRoute(user, value).getDescription());
	}
}
