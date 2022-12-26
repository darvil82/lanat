package argparser.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

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
		// remove all non-alphanumeric characters
		return UtlString.trim(name.replaceAll("[^a-zA-Z0-9 -]", ""), "[^a-zA-Z0-9]")
			.replaceAll(" ", "-");
	}

	/**
	 * Wraps a string into multiple lines in order to fit in the given maximum width.
	 * Wrapping respects words, so no word will be split in two lines.
	 * @param str The text to wrap.
	 * @param maxWidth The maximum width that the text should never exceed.
	 * @return The wrapped text.
	 */
	public static String wrap(String str, int maxWidth) {
		final String[] words = str.split(" "); // first split all words, makes stuff easier
		final var endBuffer = new StringBuilder();
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

	/**
	 * Adds <code>padCount</code> characters (specified with <code>padChar</code>) at the left of the string.
	 * If the string has multiple lines, the padding is added on all of them.
	 * @param str The string to pad.
	 * @param padCount The amount of characters to add.
	 * @param padChar The character to use for padding.
	 * @return The padded string.
	 */
	public static String indent(String str, int padCount, char padChar) {
		final var padString = String.valueOf(padChar).repeat(padCount);
		final var endBuffer = new StringBuilder(padString);

		for (char chr : str.toCharArray()) {
			endBuffer.append(chr);
			if (chr == '\n') {
				endBuffer.append(padString);
			}
		}

		return endBuffer.toString();
	}

	/**
	 * Adds <code>padCount</code> space characters at the left of the string.
	 * If the string has multiple lines, the padding is added on all of them.
	 * @param str The string to pad.
	 * @param padCount The amount of spaces to add.
	 * @return The padded string.
	 */
	public static String indent(String str, int padCount) {
		return UtlString.indent(str, padCount, ' ');
	}

	public static String center(String str, int width, char padChar) {
		final var buffer = new StringBuilder();
		final var paddingString = String.valueOf(padChar).repeat((width / 2) - (str.length() / 2) - 1);

		buffer.append(paddingString);
		buffer.append(str);
		buffer.append(paddingString);

		return buffer.toString();
	}

	public static String trim(String str, String pattern) {
		return str.replaceAll("^" + pattern + "+", "")
			.replaceAll(pattern + "+$", "");
	}

	public static String center(String str, int width) {
		return UtlString.center(str, width, '─');
	}
}
