package argparser.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

public class UtlString {
	/**
	 * Apply a predicate for each character in the string, if any fails, return false.
	 *
	 * @param str The string to check.
	 * @param fn The predicate to apply to each character.
	 */
	public static boolean matchCharacters(String str, Predicate<Character> fn) {
		for (char chr : str.toCharArray()) {
			if (!fn.test(chr)) return false;
		}
		return true;
	}

	/**
	 * Wrap a string in two strings at both sides.
	 */
	public static String wrap(String str, String wrapper) {
		return wrapper + str + wrapper;
	}

	/**
	 * Get the longest line from the contents of a string. Lines are separated by newlines.
	 */
	public static String getLongestLine(String str) {
		return Arrays.stream(str.split("\n")).min((a, b) -> b.length() - a.length()).orElse("");
	}

	public static String sanitizeName(String name) {
		Objects.requireNonNull(name);
		return name.replaceAll("[^a-zA-Z0-9 ]", "") // first remove all prefix invalid chars
			.trim() // then trim spaces at both ends
			.replaceAll(" ", "-") // then replace spaces with dashes
			.replaceAll("=", ""); // finally remove equals signs
	}
}
