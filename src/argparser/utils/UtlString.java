package argparser.utils;

import java.util.function.Function;

public class UtlString {
	public static boolean matchCharacters(String str, Function<Character, Boolean> fn) {
		for (char chr : str.toCharArray()) {
			if (!fn.apply(chr)) return false;
		}
		return true;
	}

	public static String wrap(String str, String wrapper) {
		return wrapper + str + wrapper;
	}
}
