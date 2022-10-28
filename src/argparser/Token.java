package argparser;

import argparser.displayFormatter.TerminalDisplayer.FormattingProvider;
import argparser.utils.UtlString;

public record Token(TokenType type, String contents) implements FormattingProvider {
	public boolean isArgumentSpecifier() {
		return this.type == TokenType.ArgumentAlias || this.type == TokenType.ArgumentNameList;
	}

	@Override
	public String getFormattingSequence() {
		var contents = this.contents();
		if (contents.contains(" ")) {
			contents = UtlString.wrap(contents, "'");
		}
		return this.type.getColor().getFormattingSequence() + contents;
	}
}