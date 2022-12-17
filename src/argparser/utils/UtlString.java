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
	public static String surround(String str, String wrapper) {
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

	/**
	 * Wraps a string into multiple lines in order to fit in the given maximum width.
	 * Wrapping respects words, so no word will be split in two lines.
	 * @param str The text to wrap.
	 * @param maxWidth The maximum width that the text should never exceed.
	 * @return The wrapped text.
	 */
	public static String wrap(String str, int maxWidth) {
		String[] words = str.split(" "); // first split all words, makes stuff easier
		var endBuffer = new StringBuilder();
		int currentLength = 0; // the length of the current line

		for (var word : words) {
			var wordLength = word.length() + 1; // + 1 to account for the space

			// if the new word makes currentLength exceed the max, push it down
			if ((currentLength += wordLength) > maxWidth) {
				endBuffer.append('\n');
				currentLength = wordLength;
			}
			endBuffer.append(word).append(' ');
		}

		return endBuffer.toString();
	}
}
