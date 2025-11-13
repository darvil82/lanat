package io.github.darvil.lanat.helpgen;

import io.github.darvil.lanat.Argument;
import io.github.darvil.lanat.Group;
import io.github.darvil.lanat.helpgen.descriptions.DescriptionParser;
import io.github.darvil.terminal.textformatter.FormatOption;
import io.github.darvil.terminal.textformatter.TextFormatter;
import io.github.darvil.utils.exceptions.DisallowedInstantiationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * Contains methods for generating the help representations of {@link Group}s.
 */
public final class GroupRepr {
	private GroupRepr() {
		throw new DisallowedInstantiationException(GroupRepr.class);
	}

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
	public static @NotNull String getDescription(@NotNull Group group) {
		final var buff = new StringBuilder(GroupRepr.getName(group))
			.append(':');

		final var description = DescriptionParser.parse(group);
		if (description == null)
			return buff.toString();

		buff.append(System.lineSeparator());
		buff.append(HelpFormatter.indent(description));

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
	public static @Nullable String getDescriptions(@NotNull Group group) {
		final var buff = new StringBuilder();

		final var argDescriptions = ArgumentRepr.getDescriptions(
			Argument.sortByPriority(group.getArguments()), true
		);

		final var grpDescriptions = group.getGroups().stream()
			.map(GroupRepr::getDescriptions)
			.filter(Objects::nonNull)
			.toList();


		if (grpDescriptions.isEmpty() && argDescriptions == null)
			return null;

		if (argDescriptions != null)
			buff.append(argDescriptions);

		if (argDescriptions != null && !grpDescriptions.isEmpty())
			buff.append(System.lineSeparator());

		grpDescriptions.forEach(buff::append);

		return GroupRepr.getDescription(group)
			+ System.lineSeparator().repeat(2)
			+ HelpFormatter.indent(buff.toString())
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
	public static @NotNull String getRepresentation(@NotNull Group group) {
		final var buff = new StringBuilder();

		// its empty, nothing to append
		if (group.isEmpty()) return "";

		// if this group isn't restricted, we just want to append the arguments, basically
		if (group.isRestricted())
			buff.append('(');

		final var arguments = Argument.sortByPriority(group.getArguments()).stream()
			.filter(Argument::isVisible)
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

		final List<Group> groups = group.getGroups().stream().filter(g -> !g.isEmpty()).toList();

		if (!arguments.isEmpty() && !groups.isEmpty()) {
			buff.append(' ');
			if (group.isRestricted())
				buff.append("| ");
		}

		for (int i = 0; i < groups.size(); i++) {
			Group subGroup = groups.get(i);
			buff.append(GroupRepr.getRepresentation(subGroup)); // append the group's representation recursively
			if (i < groups.size() - 1) {
				buff.append(' ');
				if (subGroup.isRestricted())
					buff.append('|').append(' ');
			}
		}

		if (group.isRestricted())
			buff.append(')');

		return buff.toString();
	}


	/**
	 * Returns the name of the given group, formatted with bold and underline if restricted.
	 * @param group the group
	 * @return the name of the group
	 */
	public static @NotNull String getName(@NotNull Group group) {
		final var name = TextFormatter.of(group.getName())
			.addFormat(FormatOption.BOLD);

		if (group.isRestricted())
			name.addFormat(FormatOption.UNDERLINE);

		return name.toString();
	}
}