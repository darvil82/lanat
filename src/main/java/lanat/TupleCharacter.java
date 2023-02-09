package lanat;

import lanat.utils.Pair;
import org.jetbrains.annotations.NotNull;

public enum TupleCharacter {
	SQUARE_BRACKETS,
	PARENTHESIS,
	BRACES,
	ANGLE_BRACKETS;

	public @NotNull Pair<Character, Character> getCharPair() {
		return switch (this) {
			case SQUARE_BRACKETS -> new Pair<>('[', ']');
			case PARENTHESIS -> new Pair<>('(', ')');
			case BRACES -> new Pair<>('{', '}');
			case ANGLE_BRACKETS -> new Pair<>('<', '>');
		};
	}
}