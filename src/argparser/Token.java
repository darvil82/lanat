package argparser;

import argparser.displayFormatter.TextFormatter;
import argparser.utils.UtlString;

public record Token(TokenType type, String contents) implements TextFormatter {
	public boolean isArgumentSpecifier() {
		return this.type == TokenType.ArgumentAlias || this.type == TokenType.ArgumentNameList;
	}

	@Override
	public FormattingProvider getFormatting() {
		var contents = this.contents();
		if (contents.contains(" ")) {
			contents = UtlString.wrap(contents, "'");
		}
		return TextFormatter.format(this.type.getFormatting(), TextFormatter.format(contents));
	}
}