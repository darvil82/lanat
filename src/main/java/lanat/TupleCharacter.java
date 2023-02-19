package lanat;

import lanat.utils.Pair;
import org.jetbrains.annotations.NotNull;

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

	public final Pair<Character, Character> charPair;

	TupleCharacter(char open, char close) {
		this.charPair = new Pair<>(open, close);
	}
}