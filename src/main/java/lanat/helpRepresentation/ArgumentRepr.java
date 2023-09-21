package lanat.helpRepresentation;

import lanat.Argument;
import lanat.helpRepresentation.descriptions.DescriptionFormatter;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
	 * @return the representation of the argument
	 */
	public static @NotNull String getRepresentation(@NotNull Argument<?, ?> arg) {
		final var repr = arg.argType.getRepresentation();

		final var outText = new TextFormatter();
		final String names = String.join("/", arg.getNames());
		final char argPrefix = arg.getPrefix().character;

		if (arg.isRequired()) {
			outText.addFormat(FormatOption.BOLD, FormatOption.UNDERLINE);
		}

		outText.withForegroundColor(arg.getRepresentationColor());

		if (arg.isPositional() && repr != null) {
			outText.concat(repr, new TextFormatter("(" + names + ")"));
		} else {
			outText.withContents("" + argPrefix + (names.length() > 1 ? argPrefix : "") + names + (repr == null ? "" : " "));

			if (repr != null)
				outText.concat(repr);
		}

		return outText.toString();
	}

	/**
	 * Returns the {@link #getRepresentation(Argument)} and description of the given argument like shown below:
	 * <pre>
	 * &lt;representation&gt;:
	 *   &lt;description&gt;
	 * </pre>
	 *
	 * @param arg the argument
	 * @return the representation and description of the argument
	 */
	public static @Nullable String getDescription(@NotNull Argument<?, ?> arg) {
		final String desc = DescriptionFormatter.parse(arg);

		if (desc == null)
			return null;

		return ArgumentRepr.getRepresentation(arg) + ":\n" + HelpFormatter.indent(desc, arg);
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
	static String getDescriptions(@NotNull List<@NotNull Argument<?, ?>> arguments) {
		final var argDescriptions = arguments.stream().map(ArgumentRepr::getDescription).filter(Objects::nonNull).toList();
		if (argDescriptions.isEmpty())
			return "";
		final var buff = new StringBuilder();

		for (int i = 0; i < argDescriptions.size(); i++) {
			buff.append(argDescriptions.get(i));

			if (i < argDescriptions.size() - 1)
				buff.append("\n\n");
		}

		buff.append('\n');
		return buff.toString();
	}
}
