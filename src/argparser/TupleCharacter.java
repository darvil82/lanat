package argparser;

import argparser.utils.Pair;

public enum TupleCharacter {
	SquareBrackets,
	Parenthesis,
	Braces,
	AngleBrackets;

	public Pair<Character, Character> getCharPair() {
		return switch (this) {
			case SquareBrackets -> new Pair<>('[', ']');
			case Parenthesis -> new Pair<>('(', ')');
			case Braces -> new Pair<>('{', '}');
			case AngleBrackets -> new Pair<>('<', '>');
		};
	}
}