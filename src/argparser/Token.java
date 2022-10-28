package argparser;

import argparser.displayFormatter.TerminalDisplayer;
import argparser.displayFormatter.TerminalDisplayer.Color;
import argparser.displayFormatter.TerminalDisplayer.Colorable;
import argparser.displayFormatter.TerminalDisplayer.FormattingProvider;
import argparser.utils.UtlString;

enum TokenType implements Colorable {
	ArgumentAlias(Color.BrightGreen),
	ArgumentNameList(Color.BrightCyan),
	ArgumentValue(Color.BrightYellow),
	ArgumentValueTupleStart(Color.BrightMagenta),
	ArgumentValueTupleEnd(Color.BrightMagenta),
	SubCommand(Color.Blue);

	private final Color color;

	TokenType(Color color) {
		this.color = color;
	}

	@Override
	public Color getColor() {
		return this.color;
	}
}

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
		return this.type.getColor().getFormattingSequence() + contents + TerminalDisplayer.CLEAR_FORMAT;
	}
}