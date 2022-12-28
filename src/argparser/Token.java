package argparser;

import argparser.utils.displayFormatter.TextFormatter;
import argparser.utils.UtlString;

public record Token(TokenType type, String contents) {
	public TextFormatter getFormatter() {
		var contents = this.contents();
		if (contents.contains(" ")) {
			contents = UtlString.surround(contents, "'");
		}
		return new TextFormatter(contents).setColor(this.type.color);
	}
}