package argparser;

import argparser.displayFormatter.Color;

public enum TokenType {
	ArgumentAlias(Color.BrightGreen),
	ArgumentNameList(Color.BrightCyan),
	ArgumentValue(Color.BrightYellow),
	ArgumentValueTupleStart(Color.BrightMagenta),
	ArgumentValueTupleEnd(Color.BrightMagenta),
	SubCommand(Color.Blue);

	public final Color color;

	TokenType(Color color) {
		this.color = color;
	}

	public boolean isArgumentSpecifier() {
		return this == ArgumentAlias || this == ArgumentNameList;
	}

	public boolean isTuple() {
		return this == ArgumentValueTupleStart || this == ArgumentValueTupleEnd;
	}
}
