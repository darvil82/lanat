package argparser;

import argparser.utils.displayFormatter.Color;

public enum TokenType {
	ARGUMENT_NAME(Color.BRIGHT_GREEN),
	ARGUMENT_NAME_LIST(Color.BRIGHT_BLUE),
	ARGUMENT_VALUE(Color.BRIGHT_YELLOW),
	ARGUMENT_VALUE_TUPLE_START(Color.BRIGHT_MAGENTA),
	ARGUMENT_VALUE_TUPLE_END(Color.BRIGHT_MAGENTA),
	SUB_COMMAND(Color.BRIGHT_CYAN);

	public final Color color;

	TokenType(Color color) {
		this.color = color;
	}

	public boolean isArgumentSpecifier() {
		return this == ARGUMENT_NAME || this == ARGUMENT_NAME_LIST;
	}

	public boolean isTuple() {
		return this == ARGUMENT_VALUE_TUPLE_START || this == ARGUMENT_VALUE_TUPLE_END;
	}
}
