package argparser;

import argparser.displayFormatter.TerminalDisplayer.Color;
import argparser.displayFormatter.TerminalDisplayer.Colorable;

enum TokenType implements Colorable {
	ArgumentAlias(Color.BrightGreen),
	ArgumentNameList(Color.BrightCyan),
	ArgumentValue(Color.BrightYellow),
	ArgumentValueTupleStart(Color.BrightMagenta),
	ArgumentValueTupleEnd(Color.BrightMagenta),
	SubCommand(Color.BrightWhite);

	private final Color color;

	TokenType(Color color) {
		this.color = color;
	}

	@Override
	public Color getColor() {
		return this.color;
	}
}

public record Token(TokenType type, String contents) {
	public boolean isArgumentSpecifier() {
		return this.type == TokenType.ArgumentAlias || this.type == TokenType.ArgumentNameList;
	}
}