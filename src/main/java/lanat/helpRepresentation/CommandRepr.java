package lanat.helpRepresentation;

import lanat.Command;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class CommandRepr {
	private CommandRepr() {}

	public static @NotNull String getSubCommandsRepresentation(final @NotNull Command cmd) {
		return '{'
			+ String.join(" | ", cmd.getSubCommands().stream().map(CommandRepr::getCommandRepresentation).toList())
			+ '}';
	}

	public static @NotNull String getCommandRepresentation(final @NotNull Command cmd) {
		return String.join(
			"/",
			cmd.getNames().stream().map(n -> new TextFormatter(n).addFormat(FormatOption.BOLD).toString()).toList()
		);
	}

	public static @Nullable String getSubCommandsDescriptions(final @NotNull Command cmd) {
		final var subCommands = cmd.getSubCommands();
		if (subCommands.isEmpty()) return null;
		final var buff = new StringBuilder();

		for (int i = 0; i < subCommands.size(); i++) {
			var subCmd = subCommands.get(i);
			final var desc = subCmd.getDescription();
			if (desc == null) continue;

			buff.append(CommandRepr.getCommandRepresentation(subCmd)).append(":\n").append(HelpFormatter.indent(desc, cmd));

			if (i < subCommands.size() - 1) buff.append("\n\n");
		}

		return buff.toString();
	}
}
