package lanat.helpRepresentation;

import lanat.Command;
import lanat.helpRepresentation.descriptions.DescriptionParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.FormatOption;
import textFormatter.TextFormatter;

import java.util.Optional;

/**
 * Contains methods for generating the help representations of {@link Command}s.
 */
public final class CommandRepr {
	private CommandRepr() {
		throw new AssertionError("This class should not be instantiated");
	}

	/**
	 * Returns the {@link #getRepresentation(Command)} of the Sub-Commands of the given command like shown below:
	 * <pre>
	 * {&lt;sub-command1&gt; | &lt;sub-command2&gt; | ...}
	 * </pre>
	 *
	 * @param cmd the command
	 * @return the representation of the sub-commands of the command
	 */
	public static @NotNull String getSubCommandsRepresentation(@NotNull Command cmd) {
		return '{'
			+ String.join(" | ", cmd.getCommands().stream().map(CommandRepr::getRepresentation).toList())
			+ '}';
	}

	/**
	 * Returns the representation of the given command like shown below:
	 * <p>
	 * {@code <names>}
	 * <p>
	 * The names are separated by a slash.
	 *
	 * @param cmd the command
	 * @return the representation of the command
	 */
	public static @NotNull String getRepresentation(@NotNull Command cmd) {
		return String.join(
			"/",
			cmd.getNames().stream().map(n -> TextFormatter.of(n).addFormat(FormatOption.BOLD).toString()).toList()
		);
	}

	/**
	 * Returns the parsed description of the given command.
	 *
	 * @param cmd the command
	 * @return the parsed description of the command
	 */
	public static @Nullable String getDescription(@NotNull Command cmd) {
		return Optional.ofNullable(DescriptionParser.parse(cmd))
			.map(desc -> CommandRepr.getRepresentation(cmd) + ":" + System.lineSeparator() + HelpFormatter.indent(desc, cmd))
			.orElse(null);
	}

	/**
	 * Returns the name and representation of the Sub-Commands of the given command like shown below:
	 * <pre>
	 * &lt;name&gt;:
	 *   &lt;description&gt;
	 *
	 * ...
	 * </pre>
	 *
	 * @param cmd the command
	 * @return the name and representation of the sub-commands of the command
	 */
	public static @Nullable String getSubCommandsDescriptions(@NotNull Command cmd) {
		final var subCommands = cmd.getCommands();
		if (subCommands.isEmpty()) return null;
		final var buff = new StringBuilder();

		for (int i = 0; i < subCommands.size(); i++) {
			final var desc = CommandRepr.getDescription(subCommands.get(i));
			if (desc == null) continue;

			buff.append(desc);

			if (i < subCommands.size() - 1) buff.append(System.lineSeparator().repeat(2));
		}

		return buff.toString();
	}
}