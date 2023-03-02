package lanat.helpRepresentation;

import lanat.Argument;
import lanat.ArgumentParser;
import lanat.Command;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class LayoutGenerators {
	private LayoutGenerators() {}

	public static @NotNull String title(@NotNull Command cmd) {
		return cmd.getName()
			+ (cmd.getDescription() == null
				? ""
				: ":\n\n" + HelpFormatter.indent(Objects.requireNonNull(CommandRepr.getDescription(cmd)), cmd));
	}

	public static @Nullable String synopsis(@NotNull Command cmd) {
		final var args = Argument.sortByPriority(cmd.getArguments());

		if (args.isEmpty() && cmd.getSubGroups().isEmpty()) return null;
		final var buffer = new StringBuilder();

		for (var arg : args) {
			// skip arguments that are in groups (handled later)
			if (arg.getParentGroup() != null)
				continue;

			buffer.append(ArgumentRepr.getRepresentation(arg)).append(' ');
		}

		for (var group : cmd.getSubGroups()) {
			buffer.append(ArgumentGroupRepr.getRepresentation(group)).append(' ');
		}

		if (!cmd.getSubCommands().isEmpty())
			buffer.append(' ').append(CommandRepr.getSubCommandsRepresentation(cmd));

		return buffer.toString();
	}

	public static @NotNull String heading(@NotNull String content, char lineChar) {
		return UtlString.center(content, HelpFormatter.lineWrapMax, lineChar);
	}

	public static @NotNull String heading(@NotNull String content) {
		return UtlString.center(content, HelpFormatter.lineWrapMax);
	}

	public static @Nullable String argumentDescriptions(@NotNull Command cmd) {
		final var buff = new StringBuilder();
		// skip arguments that are in groups (handled later)
		final var arguments = Argument.sortByPriority(cmd.getArguments()).stream().filter(arg ->
			arg.getParentGroup() == null
		).toList();

		if (arguments.isEmpty() && cmd.getSubGroups().isEmpty()) return null;

		buff.append(ArgumentRepr.getDescriptions(arguments));

		for (var group : cmd.getSubGroups()) {
			buff.append(ArgumentGroupRepr.getDescriptions(group));
		}

		return buff.toString();
	}

	public static @Nullable String subCommandsDescriptions(@NotNull Command cmd) {
		return CommandRepr.getSubCommandsDescriptions(cmd);
	}

	public static @Nullable String programLicense(@NotNull Command cmd) {
		/* This is a bit of a special case. getLicense() is only present in ArgumentParser... It doesn't make much sense
		 * to have it in Command, since it's a program-only property. So we have to do this check here. */
		return cmd instanceof ArgumentParser ap ? ap.getLicense() : null;
	}
}