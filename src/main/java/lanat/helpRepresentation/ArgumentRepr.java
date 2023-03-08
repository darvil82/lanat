package lanat.helpRepresentation;

import lanat.Argument;
import lanat.helpRepresentation.descriptions.DescriptionFormatter;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

public final class ArgumentRepr {
	private ArgumentRepr() {}

	public static @NotNull String getRepresentation(final @NotNull Argument<?, ?> arg) {
		final var repr = arg.argType.getRepresentation();

		final var outText = new TextFormatter();
		final String names = String.join("/", arg.getNames());
		final char argPrefix = arg.getPrefix().character;

		if (arg.isObligatory()) {
			outText.addFormat(FormatOption.BOLD, FormatOption.UNDERLINE);
		}

		outText.setColor(arg.getRepresentationColor());

		if (arg.isPositional() && repr != null) {
			outText.concat(repr, new TextFormatter("(" + names + ")"));
		} else {
			outText.setContents("" + argPrefix + (names.length() > 1 ? argPrefix : "") + names + (repr == null ? "" : " "));

			if (repr != null)
				outText.concat(repr);
		}

		return outText.toString();
	}

	public static @Nullable String getDescription(final @NotNull Argument<?, ?> arg) {
		final String desc = DescriptionFormatter.parse(arg);

		if (desc == null)
			return null;

		return ArgumentRepr.getRepresentation(arg) + ":\n" + HelpFormatter.indent(desc, arg);
	}

	static String getDescriptions(final @NotNull List<@NotNull Argument<?, ?>> arguments) {
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
