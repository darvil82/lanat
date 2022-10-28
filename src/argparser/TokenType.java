package argparser;

import argparser.displayFormatter.TerminalDisplayer;

public enum TokenType implements TerminalDisplayer.Colorable {
	ArgumentAlias(TerminalDisplayer.Color.BrightGreen),
	ArgumentNameList(TerminalDisplayer.Color.BrightCyan),
	ArgumentValue(TerminalDisplayer.Color.BrightYellow),
	ArgumentValueTupleStart(TerminalDisplayer.Color.BrightMagenta),
	ArgumentValueTupleEnd(TerminalDisplayer.Color.BrightMagenta),
	SubCommand(TerminalDisplayer.Color.Blue);

	private final TerminalDisplayer.Color color;

	TokenType(TerminalDisplayer.Color color) {
		this.color = color;
	}

	@Override
	public TerminalDisplayer.Color getColor() {
		return this.color;
	}
}
