package lanat.helpRepresentation;

import lanat.Argument;
import lanat.ArgumentParser;
import lanat.Command;
import lanat.MultipleNamesAndDescription;
import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class LayoutGenerators {
	private LayoutGenerators() {}

	public static @NotNull String title(@NotNull Command cmd) {
		return cmd.getName() + (cmd.description == null ? "" : ": " + cmd.description);
	}

	public static @Nullable String synopsis(@NotNull Command cmd, boolean includeHelp) {
		final var args = Argument.sortByPriority(cmd.getArguments());

		if (args.isEmpty() && cmd.getSubGroups().isEmpty()) return null;
		final var buffer = new StringBuilder();

		for (var arg : args) {
			// skip arguments that are in groups (handled later), and help argument if it's not needed
			if (arg.getParentGroup() != null || (!includeHelp && arg.isHelpArgument()))
				continue;

			buffer.append(ArgumentRepr.getSynopsisRepresentation(arg)).append(' ');
		}

		for (var group : cmd.getSubGroups()) {
			ArgumentGroupRepr.getRepresentation(group, buffer);
			buffer.append(' ');
		}

		final var subCommands = cmd.getSubCommands();
		if (!subCommands.isEmpty()) {
			buffer.append(" {")
				.append(String.join(" | ", subCommands.stream().map(Command::getName).toList()))
				.append('}');
		}

		return buffer.toString();
	}

	public static @Nullable String synopsis(@NotNull Command cmd) {
		return synopsis(cmd, false);
	}

	public static @NotNull String heading(@NotNull String content, char lineChar) {
		return UtlString.center(content, HelpFormatter.lineWrapMax, lineChar);
	}

	public static @NotNull String heading(@NotNull String content) {
		return UtlString.center(content, HelpFormatter.lineWrapMax);
	}

	public static @Nullable String argumentDescriptions(@NotNull Command cmd) {
		final var buff = new StringBuilder();
		final var arguments = Argument.sortByPriority(cmd.getArguments()).stream().filter(arg ->
			arg.getParentGroup() == null && !arg.isHelpArgument() && arg.getDescription() != null
		).toList();

		if (arguments.isEmpty() && cmd.getSubGroups().isEmpty()) return null;

		ArgumentRepr.appendArgumentDescriptions(buff, arguments);

		for (var group : cmd.getSubGroups()) {
			buff.append(ArgumentGroupRepr.getArgumentDescriptions(group));
		}

		return buff.toString();
	}

	public static @Nullable String commandLicense(@NotNull Command cmd) {
		/* This is a bit of a special case. getLicense() is only present in ArgumentParser... It doesn't make much sense
		 * to have it in Command, since it's a program-only property. So we have to do this check here. */
		return cmd instanceof ArgumentParser ap ? ap.getLicense() : null;
	}
}