package lanat.parsing;

import io.github.darvil.terminal.textformatter.TextFormatter;
import io.github.darvil.utils.UtlString;
import org.jetbrains.annotations.NotNull;

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
		if (this.type == TokenType.ARGUMENT_VALUE) {
			if (contents.contains(" "))
				contents = '"' + UtlString.escapeQuotes(contents) + '"';
			else if (contents.isEmpty())
				contents = "\"\"";
		}
		return TextFormatter.of(contents, this.type.color);
	}
}