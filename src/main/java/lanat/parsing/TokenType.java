package lanat.parsing;

import org.jetbrains.annotations.NotNull;
import textFormatter.Color;

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

	/**
	 * Returns {@code true} if this token type represents an argument specifier. (It is either
	 * {@link #ARGUMENT_NAME} or {@link #ARGUMENT_NAME_LIST})
	 * */
	public boolean isArgumentSpecifier() {
		return this == ARGUMENT_NAME || this == ARGUMENT_NAME_LIST;
	}

	/**
	 * Returns {@code true} if this token type represents a value. (It is either
	 * {@link #ARGUMENT_VALUE} or {@link #ARGUMENT_VALUE_TUPLE_START} or
	 * {@link #ARGUMENT_VALUE_TUPLE_END})
	 * */
	public boolean isValue() {
		return this == ARGUMENT_VALUE || this.isTuple();
	}

	/**
	 * Returns {@code true} if this token type represents a tuple. (It is either
	 * {@link #ARGUMENT_VALUE_TUPLE_START} or {@link #ARGUMENT_VALUE_TUPLE_END})
	 * */
	public boolean isTuple() {
		return this == ARGUMENT_VALUE_TUPLE_START || this == ARGUMENT_VALUE_TUPLE_END;
	}
}
