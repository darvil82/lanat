package lanat;

import org.jetbrains.annotations.NotNull;

/**
 * Represents a character pair that is used to represent a tuple.
 * <h2>NOTE:</h2>
 * <p>
 * Changing the tuple characters may break compatibility with shells that use the same characters.
 * </p>
 */
public enum TupleChar {
	SQUARE_BRACKETS('[', ']'),
	PARENTHESIS('(', ')'),
	BRACES('{', '}'),
	ANGLE_BRACKETS('<', '>');

	public final char open, close;

	/** The current tuple characters used by the parser. */
	public static @NotNull TupleChar current = SQUARE_BRACKETS;

	TupleChar(char open, char close) {
		assert open != close : "The open and close characters cannot be the same.";
		this.open = open;
		this.close = close;
	}
}