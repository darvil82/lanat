package argparser.helpRepresentation;

import argparser.Argument;
import argparser.utils.displayFormatter.FormatOption;
import argparser.utils.displayFormatter.TextFormatter;

import java.util.List;

public final class ArgumentRepr {
	private ArgumentRepr() {}

	public static String getSynopsisRepresentation(argparser.Argument<?, ?> arg) {
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

	public static String getDescriptionRepresentation(argparser.Argument<?, ?> arg) {
		String desc = arg.getDescription();
		if (desc == null)
			return null;

		return ArgumentRepr.getSynopsisRepresentation(arg) + ":\n" + HelpFormatter.indent(desc, arg);
	}

	static void appendArgumentDescriptions(StringBuilder buff, List<Argument<?, ?>> arguments) {
		for (int i = 0; i < arguments.size(); i++) {
			Argument<?, ?> arg = arguments.get(i);

			buff.append(getDescriptionRepresentation(arg));

			if (i < arguments.size() - 1)
				buff.append("\n\n");
		}

		buff.append('\n');
	}
}
