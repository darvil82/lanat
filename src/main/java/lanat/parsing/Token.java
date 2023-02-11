package lanat.parsing;

import lanat.utils.UtlString;
import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;

public record Token(@NotNull TokenType type, @NotNull String contents) {
	public @NotNull TextFormatter getFormatter() {
		var contents = this.contents();
		if (contents.contains(" ") && this.type == TokenType.ARGUMENT_VALUE) {
			contents = UtlString.surround(contents, "'");
		}
		return new TextFormatter(contents, this.type.color);
	}
}