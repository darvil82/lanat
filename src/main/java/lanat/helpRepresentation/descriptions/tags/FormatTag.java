package lanat.helpRepresentation.descriptions.tags;

import lanat.NamedWithDescription;
import lanat.helpRepresentation.descriptions.Tag;
import lanat.helpRepresentation.descriptions.exceptions.MalformedTagException;
import lanat.utils.displayFormatter.FormatOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Changes the format of the text.
 * The available formats are the ones defined in {@link FormatOption}. The format name is case-insensitive.
 * <p>
 * The names that may be used are:
 * <ul>
 * <li>reset / r</li>
 * <li>bold / b</li>
 * <li>italic / i</li>
 * <li>dim / d</li>
 * <li>underline / u</li>
 * <li>blink / bl</li>
 * <li>reverse / re</li>
 * <li>hidden / h</li>
 * <li>strike / s</li>
 * </ul>
 *
 * If the format name is invalid, a {@link MalformedTagException} is thrown.
 * If no format is specified, the reset sequence is returned.
 */
public class FormatTag extends Tag {
	@Override
	protected @NotNull String parse(@NotNull NamedWithDescription user, @Nullable String value) {
		if (value == null) return FormatOption.RESET_ALL.toString();

		return (switch (value.toLowerCase()) {
			case "reset", "r" -> FormatOption.RESET_ALL;
			case "bold", "b" -> FormatOption.BOLD;
			case "italic", "i" -> FormatOption.ITALIC;
			case "dim", "d" -> FormatOption.DIM;
			case "underline", "u" -> FormatOption.UNDERLINE;
			case "blink", "bl" -> FormatOption.BLINK;
			case "reverse", "re" -> FormatOption.REVERSE;
			case "hidden", "h" -> FormatOption.HIDDEN;
			case "strike", "s" -> FormatOption.STRIKE_THROUGH;
			default -> throw new MalformedTagException("format", "unknown format name " + value);
		}).toString();
	}
}
