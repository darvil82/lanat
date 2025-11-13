package io.github.darvil.lanat.helpRepresentation.descriptions.tags;

import io.github.darvil.lanat.helpRepresentation.HelpFormatter;
import io.github.darvil.lanat.helpRepresentation.descriptions.RouteParser;
import io.github.darvil.lanat.helpRepresentation.descriptions.Tag;
import io.github.darvil.lanat.utils.NamedWithDescription;
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