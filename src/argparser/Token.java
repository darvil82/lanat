package argparser;

import argparser.displayFormatter.TextFormatter;
import argparser.utils.UtlString;

public record Token(TokenType type, String contents) {
	public boolean isArgumentSpecifier() {
		return this.type == TokenType.ArgumentAlias || this.type == TokenType.ArgumentNameList;
	}

	public TextFormatter getFormatter() {
		var contents = this.contents();
		if (contents.contains(" ")) {
			contents = UtlString.wrap(contents, "'");
		}
		return new TextFormatter(contents).setColor(this.type.color);
	}
}