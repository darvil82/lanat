package lanat.parsing;

import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import utils.UtlString;

public record Token(@NotNull TokenType type, @NotNull String contents) {
	/**
	 * Returns a {@link TextFormatter} instance that can be used to display the token.
	 * <p>
	 * This instance uses the {@link TokenType#color} property to color the token.
	 * </p>
	 * @return A {@link TextFormatter} instance that can be used to display the token.
	 */
	public @NotNull TextFormatter getFormatter() {
		var contents = this.contents();
		if (contents.contains(" ") && this.type == TokenType.ARGUMENT_VALUE) {
			contents = '"' + UtlString.escapeQuotes(contents) + '"';
		}
		return new TextFormatter(contents, this.type.color);
	}
}