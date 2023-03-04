package lanat.helpRepresentation.descriptions;

import lanat.NamedWithDescription;
import lanat.helpRepresentation.descriptions.exceptions.MalformedTagException;
import lanat.utils.displayFormatter.FormatOption;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
