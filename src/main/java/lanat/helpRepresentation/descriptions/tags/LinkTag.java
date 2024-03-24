package lanat.helpRepresentation.descriptions.tags;

import lanat.helpRepresentation.HelpFormatter;
import lanat.helpRepresentation.descriptions.RouteParser;
import lanat.helpRepresentation.descriptions.Tag;
import lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Gets the representation of the target object specified by the route.
 * @see RouteParser
 * @see HelpFormatter#getRepresentation(NamedWithDescription)
 */
public class LinkTag extends Tag {
	@Override
	protected @NotNull String parse(@NotNull NamedWithDescription user, @Nullable String value) {
		return HelpFormatter.getRepresentation(RouteParser.parse(user, value));
	}
}