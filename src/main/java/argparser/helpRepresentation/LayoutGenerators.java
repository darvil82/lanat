package argparser.helpRepresentation;

import argparser.Argument;
import argparser.ArgumentParser;
import argparser.Command;
import argparser.utils.UtlString;

import java.util.Arrays;

public final class LayoutGenerators {
	private LayoutGenerators() {}

	public static String title(Command cmd) {
		return cmd.name + (cmd.description == null ? "" : ": " + cmd.description);
	}

	public static String synopsis(Command cmd, boolean includeHelp) {
		final var args = Argument.sortByPriority(cmd.getArguments());

		if (args.length == 0 && cmd.getSubGroups().length == 0) return null;
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
			arg.getParentGroup() == null && !arg.isHelpArgument() && arg.getDescription() != null
		).toArray(Argument[]::new);

		if (arguments.length == 0 && cmd.getSubGroups().length == 0) return null;

		ArgumentRepr.appendArgumentDescriptions(buff, arguments);

		for (var group : cmd.getSubGroups()) {
			buff.append(ArgumentGroupRepr.getArgumentDescriptions(group));
		}

		return buff.toString();
	}

	public static String commandLicense(Command cmd) {
		/* This is a bit of a special case. getLicense() is only present in ArgumentParser... It doesn't make much sense
		 * to have it in Command, since it's a program-only property. So we have to do this check here. */
		return cmd instanceof ArgumentParser ? ((ArgumentParser)cmd).getLicense() : null;
	}
}