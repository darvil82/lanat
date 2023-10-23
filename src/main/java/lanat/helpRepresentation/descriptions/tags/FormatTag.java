package lanat.helpRepresentation.descriptions.tags;

import lanat.NamedWithDescription;
import lanat.helpRepresentation.descriptions.Tag;
import lanat.helpRepresentation.descriptions.exceptions.MalformedTagException;
import lanat.utils.UtlString;
import lanat.utils.displayFormatter.FormatOption;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Changes the format of the text. (e.g. {@code <format=bold>}). The available formats are the ones defined in
 * {@link FormatOption}. The format name is case-insensitive.
 * <p>
 * The names that may be used are:
 * <ul>
 * <li>reset</li>
 * <li>bold / b</li>
 * <li>italic / i</li>
 * <li>dim / d</li>
 * <li>underline / u</li>
 * <li>blink / bl</li>
 * <li>reverse / r</li>
 * <li>hidden / h</li>
 * <li>strike / s</li>
 * </ul>
 *
 * <p>
 * The tag can receive multiple format names, separated by commas. (e.g. {@code <format=bold,italic>}).
 * If the format name is preceded by an exclamation mark, the format of that kind will be reset. (e.g. {@code <format=!bold>}).
 * Both can be used together. For example, in {@code <format=bold,!italic>}), the text will be bold and the italic format will be reset.
 * </p>
 * <p>
 * If the format name is invalid, a {@link MalformedTagException} is thrown.
 * If no format is specified, the reset sequence is returned. (e.g. {@code <format>}).
 * </p>
 */
public class FormatTag extends Tag {
	@Override
	protected @NotNull String parse(@NotNull NamedWithDescription user, @Nullable String value) {
		if (!TextFormatter.enableSequences) return "";
		if (value == null) return FormatOption.RESET_ALL.seq();

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
			case "dim", "d" -> FormatOption.DIM;
			case "underline", "u" -> FormatOption.UNDERLINE;
			case "blink", "bl" -> FormatOption.BLINK;
			case "reverse", "r" -> FormatOption.REVERSE;
			case "hidden", "h" -> FormatOption.HIDDEN;
			case "strike", "s" -> FormatOption.STRIKE_THROUGH;
			default ->
				throw new MalformedTagException(FormatTag.class, "unknown format name '" + formatName + "'");
		};
	}
}
