package lanat.helpRepresentation;

import lanat.Argument;
import lanat.ArgumentGroup;
import lanat.helpRepresentation.descriptions.DescriptionFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.FormatOption;
import textFormatter.TextFormatter;

import java.util.List;
import java.util.Objects;

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
	 *
	 * @param group the group
	 * @return the name and description of the group
	 */
	public static @NotNull String getDescription(@NotNull ArgumentGroup group) {
		final var buff = new StringBuilder();

		final var name = TextFormatter.of(group.getName() + ':').addFormat(FormatOption.BOLD);
		if (group.isRestricted())
			name.addFormat(FormatOption.UNDERLINE);

		buff.append(name);

		final var description = DescriptionFormatter.parse(group);
		if (description == null)
			return buff.toString();

		buff.append(System.lineSeparator());
		buff.append(HelpFormatter.indent(description, group));

		return buff.toString();
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
	 *
	 * @param group the group
	 * @return the descriptions of the arguments and subgroups of the group
	 */
	public static @Nullable String getDescriptions(@NotNull ArgumentGroup group) {
		final var buff = new StringBuilder();

		final var argDescriptions = ArgumentRepr.getDescriptions(
			Argument.sortByPriority(group.getArguments()), true
		);

		final var grpDescriptions = group.getGroups().stream()
			.map(ArgumentGroupRepr::getDescriptions)
			.filter(Objects::nonNull)
			.toList();


		if (grpDescriptions.isEmpty() && argDescriptions == null)
			return null;

		if (argDescriptions != null)
			buff.append(argDescriptions);

		if (argDescriptions != null && !grpDescriptions.isEmpty())
			buff.append(System.lineSeparator());

		grpDescriptions.forEach(buff::append);

		return ArgumentGroupRepr.getDescription(group)
			+ System.lineSeparator().repeat(2)
			+ HelpFormatter.indent(buff.toString(), group)
			+ System.lineSeparator();
	}


	/**
	 * Returns the representation of the given group like shown below:
	 * <pre>
	 * &lt;arguments&gt;
	 * </pre>
	 * The arguments are sorted by priority.
	 * @param group the group
	 */
	public static @NotNull String getRepresentation(@NotNull ArgumentGroup group) {
		final var buff = new StringBuilder();

		// its empty, nothing to append
		if (group.isEmpty()) return "";

		// if this group isn't restricted, we just want to append the arguments, basically
		if (group.isRestricted())
			buff.append('(');

		final var arguments = Argument.sortByPriority(group.getArguments()).stream()
			.filter(arg -> !arg.isHidden())
			.toList();

		for (int i = 0; i < arguments.size(); i++) {
			Argument<?, ?> arg = arguments.get(i);

			buff.append(ArgumentRepr.getRepresentation(arg, false));
			if (i < arguments.size() - 1) {
				buff.append(' ');
				if (group.isRestricted())
					buff.append('|').append(' ');
			}
		}

		final List<ArgumentGroup> groups = group.getGroups().stream().filter(g -> !g.isEmpty()).toList();

		if (!arguments.isEmpty() && !groups.isEmpty()) {
			buff.append(' ');
			if (group.isRestricted())
				buff.append("| ");
		}

		for (int i = 0; i < groups.size(); i++) {
			ArgumentGroup grp = groups.get(i);
			buff.append(ArgumentGroupRepr.getRepresentation(grp)); // append the group's representation recursively
			if (i < groups.size() - 1) {
				buff.append(' ');
				if (grp.isRestricted())
					buff.append('|').append(' ');
			}
		}

		if (group.isRestricted())
			buff.append(')');

		return buff.toString();
	}
}