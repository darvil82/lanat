package lanat.helpRepresentation;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.helpRepresentation.descriptions.DescriptionFormatter;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Contains methods for generating the help representations of {@link ArgumentGroup}s.
 */
public final class ArgumentGroupRepr {
	private ArgumentGroupRepr() {}

	/**
	 * Returns the name and description of the given group like shown below:
	 * <pre>
	 * &lt;name&gt;:
	 *   &lt;description&gt;
	 * </pre>
	 * @param group the group
	 * @return the name and description of the group
	 */
	public static @Nullable String getDescription(@NotNull ArgumentGroup group) {
		final var description = DescriptionFormatter.parse(group);
		if (description == null)
			return null;

		final var name = new TextFormatter(group.name + ':').addFormat(FormatOption.BOLD);
		if (group.isExclusive())
			name.addFormat(FormatOption.UNDERLINE);

		return '\n' + name.toString() + '\n' + HelpFormatter.indent(description, group);
	}

	/**
	 * Returns the descriptions of the arguments and subgroups of the given group like shown below:
	 * <pre>
	 * &lt;name&gt;:
	 *   &lt;description&gt;
	 *
	 *   &lt;argument descriptions&gt;
	 *
	 *   &lt;subgroup descriptions&gt;
	 * </pre>
	 * @param group the group
	 * @return the descriptions of the arguments and subgroups of the group
	 */
	public static @NotNull String getDescriptions(@NotNull ArgumentGroup group) {
		final var arguments = Argument.sortByPriority(group.getArguments());
		final var buff = new StringBuilder();
		final var name = new TextFormatter(group.name + ':').addFormat(FormatOption.BOLD);
		final var description = DescriptionFormatter.parse(group);
		final var argumentDescriptions = ArgumentRepr.getDescriptions(arguments);

		if (description == null && argumentDescriptions.isEmpty())
			return "";

		if (group.isExclusive())
			name.addFormat(FormatOption.UNDERLINE);

		if (description != null)
			buff.append(description).append("\n\n");

		buff.append(ArgumentRepr.getDescriptions(arguments));

		for (final var subGroup : group.getSubGroups()) {
			buff.append(ArgumentGroupRepr.getDescriptions(subGroup));
		}

		return '\n' + name.toString() + '\n' + HelpFormatter.indent(buff.toString(), group);
	}


	/**
	 * Returns the representation of the given group like shown below:
	 * <pre>
	 * &lt;name&gt; &lt;arguments&gt;
	 * </pre>
	 * @param group the group
	 */
	public static String getRepresentation(@NotNull ArgumentGroup group) {
		final var sb = new StringBuilder();

		// its empty, nothing to append
		if (group.isEmpty()) return "";

		// if this group isn't exclusive, we just want to append the arguments, basically
		if (group.isExclusive())
			sb.append('(');

		final var arguments = Argument.sortByPriority(group.getArguments());
		for (int i = 0; i < arguments.size(); i++) {
			Argument<?, ?> arg = arguments.get(i);

			sb.append(ArgumentRepr.getRepresentation(arg));
			if (i < arguments.size() - 1) {
				sb.append(' ');
				if (group.isExclusive())
					sb.append('|').append(' ');
			}
		}

		final List<ArgumentGroup> groups = group.getSubGroups().stream().filter(g -> !g.isEmpty()).toList();

		if (!arguments.isEmpty() && !groups.isEmpty()) {
			sb.append(' ');
			if (group.isExclusive())
				sb.append("| ");
		}

		for (int i = 0; i < groups.size(); i++) {
			ArgumentGroup grp = groups.get(i);
			sb.append(ArgumentGroupRepr.getRepresentation(grp)); // append the group's representation recursively
			if (i < groups.size() - 1) {
				sb.append(' ');
				if (grp.isExclusive())
					sb.append('|').append(' ');
			}
		}

		if (group.isExclusive())
			sb.append(')');

		return sb.toString();
	}
}
