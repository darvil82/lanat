package lanat.helpRepresentation.descriptions;

import lanat.CommandUser;
import org.jetbrains.annotations.NotNull;

public class LinkTag extends Tag {

	public LinkTag(@NotNull String value) {
		super(value);
	}

	@Override
	protected @NotNull String parse(@NotNull CommandUser user) {
		return "testing";
	}
}
