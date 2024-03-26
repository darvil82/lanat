package lanat.helpRepresentation.descriptions.tags;

import lanat.helpRepresentation.descriptions.Tag;
import lanat.helpRepresentation.descriptions.exceptions.MalformedTagException;
import lanat.utils.NamedWithDescription;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import textFormatter.FormatOption;
import textFormatter.TextFormatter;
import utils.UtlString;

/**
 * Changes the format of the text. (e.g. {@code <format=bold>}). The available formats are the ones defined in
 * {@link FormatOption}. The format value is case-insensitive.
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
 * The tag can receive multiple format values, separated by commas. (e.g. {@code <format=bold,italic>}).
 * If the format value is preceded by an exclamation mark, the format of that kind will be reset. (e.g. {@code <format=!bold>}).
 * Both can be used together. For example, in {@code <format=bold,!italic>}), the text will be bold and the italic format will be reset.
 * </p>
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