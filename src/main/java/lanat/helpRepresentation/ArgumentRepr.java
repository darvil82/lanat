package lanat.helpRepresentation;

import lanat.Argument;
import lanat.helpRepresentation.descriptions.DescriptionParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.FormatOption;
import textFormatter.TextFormatter;

import java.util.List;
import java.util.Objects;

/**
 * Contains methods for generating the help representations of {@link Argument}s.
 */
public final class ArgumentRepr {
	private ArgumentRepr() {}

	/**
	 * Returns the representation of the given argument like shown below:
	 * <p>
	 * {@code <prefix><names> <type_representation>}
	 * <p>
	 * or
	 * <p>
	 * {@code <type_representation> (<names>)}: if the argument is positional
	 *
	 * @param arg the argument
	 * @param multipleNames if {@code true}, multiple names will be shown.
	 *   If {@code false}, only the longest name will be shown
	 * @return the representation of the argument
	 */
	public static @NotNull String getRepresentation(@NotNull Argument<?, ?> arg, boolean multipleNames) {
		final var repr = arg.type.getRepresentation();

		final var outText = TextFormatter.create();

		if (arg.isRequired()) {
			outText.addFormat(FormatOption.BOLD, FormatOption.UNDERLINE);
		}

		outText.withForegroundColor(arg.getRepresentationColor());

		// if it is positional, a slash should split the names
		if (arg.isPositional() && repr != null) {
			return outText
				.concat(repr)
				.concat("(" + (multipleNames ? String.join("/", arg.getNames()) : arg.getName()) + ")")
				.toString();
		}

		// if multipleNames, show each name separated by a comma including the prefix
		if (multipleNames) {
			var buff = new StringBuilder();
			var names = arg.getNames().stream().map(name -> arg.getPrefix() + name).toList();

			for (int i = 0; i < names.size(); i++) {
				buff.append(names.get(i));

				if (i < names.size() - 1)
					buff.append(", ");
			}

			outText.withContents(buff.toString());
		} else {
			outText.withContents(arg.getPrefix() + arg.getName());
		}

		if (repr != null)
			outText.concat(" ").concat(repr);

		return outText.toString();
	}

	/**
	 * Returns the {@link #getRepresentation(Argument, boolean)} and description of the given argument like shown below:
	 * <pre>
	 * &lt;representation&gt;:
	 *   &lt;description&gt;
	 * </pre>
	 *
	 * @param arg the argument
	 * @return the representation and description of the argument
	 */
	public static @Nullable String getDescription(@NotNull Argument<?, ?> arg, boolean forceName) {
		var description = DescriptionParser.parse(arg);

		if (description == null) {
			if (forceName)
				return ArgumentRepr.getRepresentation(arg, true);
			return null;
		}

		return ArgumentRepr.getRepresentation(arg, true) + ":"
			+ System.lineSeparator()
			+ HelpFormatter.indent(description, arg)
			+ System.lineSeparator();
	}

	/**
	 * Returns the descriptions of the given arguments like shown below:
	 * <pre>
	 * &lt;description&gt;
	 *
	 * &lt;description&gt;
	 *
	 * ...
	 * </pre>
	 *
	 * @param arguments the arguments
	 * @return the descriptions of the arguments
	 */
	public static @Nullable String getDescriptions(@NotNull List<@NotNull Argument<?, ?>> arguments, boolean forceNames) {
		final var argDescriptions = arguments.stream()
			.filter(arg -> !arg.isHidden())
			.map(arg -> ArgumentRepr.getDescription(arg, forceNames))
			.filter(Objects::nonNull)
			.toList();

		if (argDescriptions.isEmpty())
			return null;

		final var buff = new StringBuilder();

		for (int i = 0; i < argDescriptions.size(); i++) {
			buff.append(argDescriptions.get(i));

			if (i < argDescriptions.size() - 1)
				buff.append(System.lineSeparator());
		}

		return buff.toString();
	}
}