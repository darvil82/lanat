package lanat.parsing;

import lanat.utils.displayFormatter.Color;
import org.jetbrains.annotations.NotNull;

public enum TokenType {
	ARGUMENT_NAME(Color.BRIGHT_GREEN),
	ARGUMENT_NAME_LIST(Color.BRIGHT_BLUE),
	ARGUMENT_VALUE(Color.BRIGHT_YELLOW),
	ARGUMENT_VALUE_TUPLE_START(Color.BRIGHT_MAGENTA),
	ARGUMENT_VALUE_TUPLE_END(Color.BRIGHT_MAGENTA),
	COMMAND(Color.BRIGHT_CYAN),
	FORWARD_VALUE(Color.GRAY);

	public final @NotNull Color color;

	TokenType(@NotNull Color color) {
		this.color = color;
	}

	public boolean isArgumentSpecifier() {
		return this == ARGUMENT_NAME || this == ARGUMENT_NAME_LIST;
	}

	public boolean isTuple() {
		return this == ARGUMENT_VALUE_TUPLE_START || this == ARGUMENT_VALUE_TUPLE_END;
	}
}
