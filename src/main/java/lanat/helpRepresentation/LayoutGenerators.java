package lanat.helpRepresentation;

import io.github.darvil.utils.UtlString;
import io.github.darvil.utils.exceptions.DisallowedInstantiationException;
import lanat.Argument;
import lanat.ArgumentParser;
import lanat.Command;
import lanat.Group;
import lanat.helpRepresentation.descriptions.DescriptionParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * This contains methods that may be used in {@link LayoutItem}s to generate the content of the help message.
 *
 * @see LayoutItem
 */
public final class LayoutGenerators {
	private LayoutGenerators() {
		throw new DisallowedInstantiationException(LayoutGenerators.class);
	}

	/**
	 * Shows the title of the command, followed by a description, if any.
	 *
	 * @param cmd The command to generate the title for.
	 * @return the generated title and description.
	 */
	public static @NotNull String titleAndDescription(@NotNull Command cmd) {
		final var buff = new StringBuilder(CommandRepr.getRepresentation(cmd));

		if (cmd instanceof ArgumentParser ap) {
			Optional.ofNullable(ap.getVersion())
				.ifPresent(version -> buff.append(" (").append(version).append(')'));
		}

		Optional.ofNullable(DescriptionParser.parse(cmd))
			.ifPresent(desc ->
				buff.append(":").append(System.lineSeparator().repeat(2))
					.append(HelpFormatter.indent(desc))
			);

		return buff.toString();
	}

	/**
	 * Shows the synopsis of the command, if any.
	 * <p>
	 * The synopsis is a list of all {@link Argument}s, {@link Group}s and Sub-{@link Command}s of the
	 * command. Each is shown with its own representation, as defined by the {@link ArgumentRepr},
	 * {@link GroupRepr} and {@link CommandRepr} classes.
	 * </p>
	 * <p>
	 * First elements shown are the arguments, ordered by {@link Argument#sortByPriority(List)}, then the
	 * {@link Group}s, which are shown recursively, and finally the sub-commands.
	 * </p>
	 *
	 * @param cmd The command to generate the synopsis for.
	 * @return the generated synopsis.
	 */
	public static @Nullable String synopsis(@NotNull Command cmd) {
		final var args = Argument.sortByPriority(cmd.getArguments()).stream()
			.filter(arg -> arg.getParentGroup() == null)
			.filter(Argument::isVisible)
			.toList();

		if (args.isEmpty() && cmd.getGroups().isEmpty()) return null;
		final var buffer = new StringBuilder();

		args.forEach(arg -> buffer.append(ArgumentRepr.getRepresentation(arg, false)).append(' '));
		cmd.getGroups().forEach(group -> buffer.append(GroupRepr.getRepresentation(group)).append(' '));

		if (!cmd.getCommands().isEmpty())
			buffer.append(CommandRepr.getSubCommandsRepresentation(cmd));

		return buffer.toString();
	}

	/**
	 * @param content Shows a heading with the given content, centered and surrounded by the given character.
	 * @param lineChar The character to surround the content with.
	 * @return the generated heading.
	 */
	public static @NotNull String heading(@NotNull String content, char lineChar) {
		return UtlString.center(content, HelpFormatter.getLineWrapMax(), lineChar);
	}

	/**
	 * Shows a heading with the given content, centered and surrounded by dashes.
	 *
	 * @param content The content of the heading.
	 * @return the generated heading.
	 */
	public static @NotNull String heading(@NotNull String content) {
		return UtlString.center(content, HelpFormatter.getLineWrapMax(), '─');
	}

	/**
	 * Shows the descriptions of the {@link Argument}s and {@link Group}s of the command.
	 * <p>
	 * The descriptions are shown in the same order as the synopsis. If groups are present, they are shown recursively
	 * too, with their own descriptions and with the correct indentation level.
	 * </p>
	 *
	 * @param cmd The command to generate the descriptions for.
	 * @return the generated descriptions.
	 */
	public static @Nullable String argumentsDescriptions(@NotNull Command cmd) {
		final var buff = new StringBuilder();
		// skip arguments that are in groups (handled later)
		final var arguments = Argument.sortByPriority(cmd.getArguments()).stream()
			.filter(arg -> arg.getParentGroup() == null)
			.toList();

		if (arguments.isEmpty() && cmd.getGroups().isEmpty()) return null;

		Optional.ofNullable(ArgumentRepr.getDescriptions(arguments, false))
			.ifPresent(buff::append);

		final var groups = cmd.getGroups().stream()
			.filter(Group::isRoot)
			.map(GroupRepr::getDescriptions)
			.filter(Objects::nonNull)
			.toList();

		if (!groups.isEmpty())
			buff.append(System.lineSeparator().repeat(1));

		groups.forEach(buff::append);

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
	 * Shows the details of the command, if any.
	 * <p>
	 * Note that this is a program-only property, so it will only be shown if the command is an instance of
	 * {@link ArgumentParser}, that is, if it is the root command.
	 * @param cmd The command to generate the details message for.
	 * @return the generated details message.
	 * @see ArgumentParser#setDetails(String)
	 */
	public static @Nullable String programDetails(@NotNull Command cmd) {
		/* This is a bit of a special case. getDetails() is only present in ArgumentParser... It makes little sense
		 * to have it in Command, since it's a program-only property. So we have to do this check here. */
		return cmd instanceof ArgumentParser ap ? ap.getDetails() : null;
	}
}