package lanat.parsing;

import org.jetbrains.annotations.NotNull;
import textFormatter.color.Color;
import textFormatter.color.SimpleColor;

public enum TokenType {
	ARGUMENT_NAME(SimpleColor.BRIGHT_GREEN),
	ARGUMENT_NAME_LIST(SimpleColor.BRIGHT_BLUE),
	ARGUMENT_VALUE(SimpleColor.BRIGHT_YELLOW),
	ARGUMENT_VALUE_TUPLE_START(SimpleColor.BRIGHT_MAGENTA),
	ARGUMENT_VALUE_TUPLE_END(SimpleColor.BRIGHT_MAGENTA),
	COMMAND(SimpleColor.BRIGHT_CYAN),
	FORWARD_VALUE(SimpleColor.GRAY);

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