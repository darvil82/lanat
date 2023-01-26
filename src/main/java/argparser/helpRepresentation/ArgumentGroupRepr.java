package argparser.helpRepresentation;

import argparser.Argument;
import argparser.ArgumentGroup;
import argparser.utils.UtlString;
import argparser.utils.displayFormatter.FormatOption;
import argparser.utils.displayFormatter.TextFormatter;

import java.util.Arrays;
import java.util.List;

public abstract class ArgumentGroupRepr {
	public static String getArgumentDescriptions(argparser.ArgumentGroup group) {
		final var arguments = argparser.Argument.sortByPriority(group.getArguments());
		final var buff = new StringBuilder();
		final var name = new TextFormatter(group.name + ':').addFormat(FormatOption.BOLD);

		if (group.isExclusive())
			name.addFormat(FormatOption.UNDERLINE);

		for (Argument<?, ?> arg : arguments) {
			buff.append(ArgumentRepr.getDescriptionRepresentation(arg)).append('\n');
		}

		for (final var subGroup : group.getSubGroups()) {
			buff.append(ArgumentGroupRepr.getArgumentDescriptions(subGroup));
		}

		return '\n' + name.toString() + '\n' + UtlString.indent(buff.toString(), 3);
	}

	/**
	 * Appends the representation of this group tree to the given string builder.
	 */
	public static void getRepresentation(argparser.ArgumentGroup group, StringBuilder sb) {
		// its empty, nothing to append
		if (group.isEmpty()) return;

		// if this group isn't exclusive, we just want to append the arguments, basically
		if (group.isExclusive())
			sb.append('(');

		final Argument<?, ?>[] arguments = Argument.sortByPriority(group.getArguments());
		for (int i = 0; i < arguments.length; i++) {
			Argument<?, ?> arg = arguments[i];

			sb.append(ArgumentRepr.getSynopsisRepresentation(arg));
			if (i < arguments.length - 1) {
				sb.append(' ');
				if (group.isExclusive())
					sb.append('|').append(' ');
			}
		}

		final List<ArgumentGroup> groups = Arrays.stream(group.getSubGroups()).filter(g -> !g.isEmpty()).toList();

		if (arguments.length != 0 && !groups.isEmpty()) {
			sb.append(' ');
			if (group.isExclusive())
				sb.append("| ");
		}

		for (int i = 0; i < groups.size(); i++) {
			ArgumentGroup grp = groups.get(i);
			ArgumentGroupRepr.getRepresentation(grp, sb); // append the group's representation recursively
			if (i < groups.size() - 1) {
				sb.append(' ');
				if (grp.isExclusive())
					sb.append('|').append(' ');
			}
		}

		if (group.isExclusive())
			sb.append(')');
	}
}
