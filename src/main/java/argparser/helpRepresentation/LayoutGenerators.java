package argparser.helpRepresentation;

import argparser.Argument;
import argparser.Command;
import argparser.utils.UtlString;

import java.util.Arrays;

public abstract class LayoutGenerators {
	public static String title(Command cmd) {
		return cmd.name + (cmd.description == null ? "" : ": " + cmd.description);
	}

	public static String synopsis(Command cmd, boolean includeHelp) {
		final var args = Argument.sortByPriority(cmd.getArguments());

		if (args.length == 0 && cmd.getSubGroups().length == 0) return "";
		final var buffer = new StringBuilder();

		for (var arg : args) {
			String representation;

			// skip arguments that are in groups (handled later), and help argument if it's not needed
			if (
				arg.getParentGroup() != null
					|| (!includeHelp && arg.isHelpArgument())
					|| (representation = ArgumentRepr.getSynopsisRepresentation(arg)) == null
			)
				continue;

			buffer.append(representation).append(' ');
		}

		for (var group : cmd.getSubGroups()) {
			ArgumentGroupRepr.getRepresentation(group, buffer);
			buffer.append(' ');
		}

		final var subCommands = cmd.getSubCommands();
		if (subCommands.length > 0) {
			buffer.append(" {")
				.append(String.join(" | ", Arrays.stream(subCommands).map(c -> c.name).toList()))
				.append('}');
		}

		return buffer.toString();
	}

	public static String synopsis(Command cmd) {
		return synopsis(cmd, false);
	}

	public static String heading(String content, char lineChar) {
		return UtlString.center(content, HelpFormatter.lineWrapMax, lineChar);
	}

	public static String heading(String content) {
		return UtlString.center(content, HelpFormatter.lineWrapMax);
	}

	public static String argumentDescriptions(Command cmd) {
		final var buff = new StringBuilder();
		final var arguments = Arrays.stream(Argument.sortByPriority(cmd.getArguments())).filter(arg ->
			arg.getParentGroup() == null && !arg.isHelpArgument()
		).toArray(Argument[]::new);

		if (arguments.length == 0 && cmd.getSubGroups().length == 0) return "";

		for (int i = 0; i < arguments.length; i++) {
			Argument<?, ?> arg = arguments[i];
			if (arg.getParentGroup() != null) continue;

			final var argDesc = ArgumentRepr.getDescriptionRepresentation(arg);
			if (argDesc == null) continue;

			buff.append(argDesc);
			if (i < arguments.length - 1)
				buff.append("\n\n");
		}

		buff.append('\n');

		for (var group : cmd.getSubGroups()) {
			buff.append(ArgumentGroupRepr.getArgumentDescriptions(group));
		}

		return buff.toString();
	}
}