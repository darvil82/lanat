package lanat;

import lanat.utils.Pair;

public enum TupleCharacter {
	SQUARE_BRACKETS,
	PARENTHESIS,
	BRACES,
	ANGLE_BRACKETS;

	public Pair<Character, Character> getCharPair() {
		return switch (this) {
			case SQUARE_BRACKETS -> new Pair<>('[', ']');
			case PARENTHESIS -> new Pair<>('(', ')');
			case BRACES -> new Pair<>('{', '}');
			case ANGLE_BRACKETS -> new Pair<>('<', '>');
		};
	}
}