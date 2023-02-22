package lanat.helpRepresentation;

import lanat.Command;
import org.jetbrains.annotations.NotNull;

public final class CommandRepr {
	private CommandRepr() {}

	public static @NotNull String getSubcommandsRepresentation(final @NotNull Command cmd) {
		final var subCommands = cmd.getSubCommands();
		if (!subCommands.isEmpty()) {
			return " {"
				+ String.join(" | ", subCommands.stream().map(c -> String.join("/", c.getNames())).toList())
				+ '}';
		}
		return "";
	}
}
