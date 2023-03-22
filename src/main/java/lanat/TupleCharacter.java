package lanat;

/**
 * Represents a character pair that is used to represent a tuple.
 * <h2>NOTE:</h2>
 * <p>
 * Changing the tuple characters may break compatibility with shells that use the same characters.
 * </p>
 */
public enum TupleCharacter {
	SQUARE_BRACKETS('[', ']'),
	PARENTHESIS('(', ')'),
	BRACES('{', '}'),
	ANGLE_BRACKETS('<', '>');

	public final char open, close;

	TupleCharacter(char open, char close) {
		this.open = open;
		this.close = close;
	}
}