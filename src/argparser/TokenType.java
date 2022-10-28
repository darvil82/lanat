package argparser;

import argparser.displayFormatter.TextFormatter;

public enum TokenType implements TextFormatter {
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
	public FormattingProvider getFormatting() {
		return this.color;
	}
}
