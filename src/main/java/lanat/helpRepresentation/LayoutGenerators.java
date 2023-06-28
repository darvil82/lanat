package lanat.helpRepresentation;

import lanat.Argument;
import lanat.ArgumentParser;
import lanat.Command;
import lanat.helpRepresentation.descriptions.DescriptionFormatter;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * This contains methods that may be used in {@link LayoutItem}s to generate the content of the help message.
 *
 * @see LayoutItem
 */
public final class LayoutGenerators {
	private LayoutGenerators() {}

	/**
	 * Shows the title of the command, followed by a description, if any.
	 *
	 * @param cmd The command to generate the title for.
	 * @return the generated title and description.
	 */
	public static @NotNull String titleAndDescription(@NotNull Command cmd) {
		final var description = DescriptionFormatter.parse(cmd);
		final var buff = new StringBuilder(CommandRepr.getRepresentation(cmd));

		if (cmd instanceof ArgumentParser ap) {
			final var version = ap.getVersion();
			if (version != null) {
				buff.append(" (").append(version).append(')');
			}
		}

		if (description != null) {
			buff.append(":\n\n");
			buff.append(HelpFormatter.indent(description, cmd));
		}

		return buff.toString();
	}

	/**
	 * Shows the synopsis of the command, if any.
	 * <p>
	 * The synopsis is a list of all {@link Argument}s, {@link lanat.ArgumentGroup}s and Sub-{@link Command}s of the
	 * command. Each is shown with its own representation, as defined by the {@link ArgumentRepr},
	 * {@link ArgumentGroupRepr} and {@link CommandRepr} classes.
	 * </p>
	 * <p>
	 * First elements shown are the arguments, ordered by {@link Argument#sortByPriority(List)}, then the
	 * {@link lanat.ArgumentGroup}s, which are shown recursively, and finally the sub-commands.
	 * </p>
	 *
	 * @param cmd The command to generate the synopsis for.
	 * @return the generated synopsis.
	 */
	public static @Nullable String synopsis(@NotNull Command cmd) {
		final var args = Argument.sortByPriority(cmd.getArguments());

		if (args.isEmpty() && cmd.getGroups().isEmpty()) return null;
		final var buffer = new StringBuilder();

		for (var arg : args) {
			// skip arguments that are in groups (handled later)
			if (arg.getParentGroup() != null)
				continue;

			buffer.append(ArgumentRepr.getRepresentation(arg)).append(' ');
		}

		for (var group : cmd.getGroups()) {
			buffer.append(ArgumentGroupRepr.getRepresentation(group)).append(' ');
		}

		if (!cmd.getCommands().isEmpty())
			buffer.append(' ').append(CommandRepr.getSubCommandsRepresentation(cmd));

		return buffer.toString();
	}

	/**
	 * @param content Shows a heading with the given content, centered and surrounded by the given character.
	 * @param lineChar The character to surround the content with.
	 * @return the generated heading.
	 */
	public static @NotNull String heading(@NotNull String content, char lineChar) {
		return UtlString.center(content, HelpFormatter.lineWrapMax, lineChar);
	}

	/**
	 * Shows a heading with the given content, centered and surrounded by dashes.
	 *
	 * @param content The content of the heading.
	 * @return the generated heading.
	 */
	public static @NotNull String heading(@NotNull String content) {
		return UtlString.center(content, HelpFormatter.lineWrapMax);
	}

	/**
	 * Shows the descriptions of the {@link Argument}s and {@link lanat.ArgumentGroup}s of the command.
	 * <p>
	 * The descriptions are shown in the same order as the synopsis. If groups are present, they are shown recursively
	 * too, with their own descriptions and with the correct indentation level.
	 * </p>
	 *
	 * @param cmd The command to generate the descriptions for.
	 * @return the generated descriptions.
	 */
	public static @Nullable String argumentDescriptions(@NotNull Command cmd) {
		final var buff = new StringBuilder();
		// skip arguments that are in groups (handled later)
		final var arguments = Argument.sortByPriority(cmd.getArguments()).stream().filter(arg ->
			arg.getParentGroup() == null
		).toList();

		if (arguments.isEmpty() && cmd.getGroups().isEmpty()) return null;

		buff.append(ArgumentRepr.getDescriptions(arguments));

		for (var group : cmd.getGroups()) {
			buff.append(ArgumentGroupRepr.getDescriptions(group));
		}

		return buff.toString();
	}

	/**
	 * Shows the descriptions of the sub-commands of the command.
	 *
	 * @param cmd The command to generate the descriptions for.
	 * @return the generated descriptions.
	 */
	public static @Nullable String subCommandsDescriptions(@NotNull Command cmd) {
		return CommandRepr.getSubCommandsDescriptions(cmd);
	}

	/**
	 * Shows the license of the command, if any.
	 * <p>
	 * Note that this is a program-only property, so it will only be shown if the command is an instance of
	 * {@link ArgumentParser}, that is, if it is the root command.
	 * </p>
	 *
	 * @param cmd The command to generate the license for.
	 * @return the generated license.
	 * @see ArgumentParser#setLicense(String)
	 */
	public static @Nullable String programLicense(@NotNull Command cmd) {
		/* This is a bit of a special case. getLicense() is only present in ArgumentParser... It doesn't make much sense
		 * to have it in Command, since it's a program-only property. So we have to do this check here. */
		return cmd instanceof ArgumentParser ap ? ap.getLicense() : null;
	}
}