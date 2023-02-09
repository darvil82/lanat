package lanat.helpRepresentation;

import lanat.Argument;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ArgumentRepr {
	private ArgumentRepr() {}

	public static @NotNull String getSynopsisRepresentation(@NotNull Argument<?, ?> arg) {
		final var repr = arg.argType.getRepresentation();

		final var outText = new TextFormatter();
		final String names = String.join("/", arg.getNames());
		final char argPrefix = arg.getPrefix();

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

	public static @Nullable String getDescriptionRepresentation(@NotNull Argument<?, ?> arg) {
		String desc = arg.getDescription();
		if (desc == null)
			return null;

		return ArgumentRepr.getSynopsisRepresentation(arg) + ":\n" + HelpFormatter.indent(desc, arg);
	}

	static void appendArgumentDescriptions(@NotNull StringBuilder buff, @NotNull List<@NotNull Argument<?, ?>> arguments) {
		for (int i = 0; i < arguments.size(); i++) {
			Argument<?, ?> arg = arguments.get(i);

			buff.append(getDescriptionRepresentation(arg));

			if (i < arguments.size() - 1)
				buff.append("\n\n");
		}

		buff.append('\n');
	}
}
