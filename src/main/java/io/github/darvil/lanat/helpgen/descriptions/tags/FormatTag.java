package io.github.darvil.lanat.helpgen.descriptions.tags;

import io.github.darvil.lanat.helpgen.descriptions.Tag;
import io.github.darvil.lanat.helpgen.descriptions.exceptions.MalformedTagException;
import io.github.darvil.lanat.utils.NamedWithDescription;
import io.github.darvil.terminal.textformatter.FormatOption;
import io.github.darvil.terminal.textformatter.TextFormatter;
import io.github.darvil.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Changes the format of the text. (e.g. {@code <format=bold>}). The available formats are the ones defined in
 * {@link FormatOption}. The format value is case-insensitive.
 * <p>
 * The syntax for specifying formats is {@code format[,format,...]} where `format` is {@code [!]format_name}.
 * {@code !} is used to reset the format of that name.
 * </p>
 * <p>
 * The values that may be used are:
 * <ul>
 * <li>reset</li>
 * <li>bold / b</li>
 * <li>italic / i</li>
 * <li>underline / u</li>
 * <li>strikethrough / s</li>
 * <li>dim</li>
 * <li>blink</li>
 * <li>reverse</li>
 * <li>hidden</li>
 * </ul>
 *
 * <p>
 * If the format value is invalid, a {@link MalformedTagException} is thrown.
 * </p>
 * @see FormatOption
 */
public class FormatTag extends Tag {
	@Override
	protected @NotNull String parse(@NotNull NamedWithDescription user, @Nullable String value) {
		if (value == null)
			throw new MalformedTagException(FormatTag.class, "no format specified");

		// if color sequences are disabled, nothing to do
		if (!TextFormatter.enableSequences) return "";

		final var buff = new StringBuilder();

		for (String opt : UtlString.split(value, ','))
			buff.append(opt.startsWith("!") ? getFormat(opt.substring(1)).reset() : getFormat(opt));

		return buff.toString();
	}

	private static FormatOption getFormat(@NotNull String formatName) {
		return switch (formatName.toLowerCase().strip()) {
			case "reset" -> FormatOption.RESET_ALL;
			case "bold", "b" -> FormatOption.BOLD;
			case "italic", "i" -> FormatOption.ITALIC;
			case "underline", "u" -> FormatOption.UNDERLINE;
			case "strikethrough", "s" -> FormatOption.STRIKETHROUGH;
			case "dim" -> FormatOption.DIM;
			case "blink"  -> FormatOption.BLINK;
			case "reverse" -> FormatOption.REVERSE;
			case "hidden" -> FormatOption.HIDDEN;
			default -> throw new MalformedTagException(FormatTag.class, "unknown format value '" + formatName + "'");
		};
	}
}