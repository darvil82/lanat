package argparser.utils;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.Predicate;

public final class UtlString {
	private UtlString() {
	}

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
		final var sanitized = UtlString.trim(name.replaceAll("[^a-zA-Z0-9 -]", ""), "[^a-zA-Z0-9]")
			.replaceAll(" ", "-");

		if (sanitized.isEmpty())
			throw new IllegalArgumentException("name must contain at least one alphanumeric character");

		return sanitized;
	}

	/**
	 * Wraps a string into multiple lines in order to fit in the given maximum width.
	 * Wrapping respects words, so no word will be split in two lines.
	 *
	 * @param str The text to wrap.
	 * @param maxWidth The maximum width that the text should never exceed.
	 * @return The wrapped text.
	 */
	public static String wrap(String str, int maxWidth) {
		// we cant split anyway, so why bother
		if (!(str.contains(" ") || str.contains("\t")))
			return str;

		if (maxWidth <= 0)
			throw new IllegalArgumentException("maxWidth must be greater than 0");

		final var wordBuff = new StringBuilder(); // buffer for the current word
		final var endBuffer = new StringBuilder(); // buffer for the final string. words are pushed here
		final var indentBuff = new StringBuilder(); // buffer for the current indentation that will be added to the beginning of each line if needed

		int lineWidth = 0; // the current line width
		boolean jumped = true; // true if a newline was added. starts off as true in case the string with indentation

		for (char chr : str.toCharArray()) {
			if (chr == ' ' || chr == '\t') {
				// if the word buffer is empty, we are at the beginning of a line, so we save this indentation
				if (wordBuff.isEmpty())
					indentBuff.append(chr);

					// if the word buffer is not empty, we are in the middle of a word, so append the word buffer to the end buffer
				else {
					// add a newline if the line width exceeds the maximum width
					if (lineWidth >= maxWidth) {
						endBuffer.append('\n').append(indentBuff);
						lineWidth = indentBuff.length();
					}
					endBuffer.append(wordBuff).append(chr);
					// make sure to not count escape sequences on the length!
					lineWidth += UtlString.removeSequences(wordBuff.toString()).length() + 1;
					wordBuff.setLength(0);
				}

				/* if the char is a newline, same as above but we reset the indentation
				 * After this character, new indentation may be added. We need to push the new one right when the
				 * next word starts, so jumped is set to true and checked below. */
			} else if (chr == '\n') {
				endBuffer.append(wordBuff).append('\n');
				wordBuff.setLength(0);
				indentBuff.setLength(0);
				jumped = true;
				lineWidth = 0;

				/* just append any other character to the word buffer.
				 * we need to append the indentation to the end buffer if we jumped to a new line */
			} else {
				if (jumped) {
					endBuffer.append(indentBuff);
					jumped = false;
					lineWidth = indentBuff.length();
				}
				wordBuff.append(chr);
			}
		}

		if (!wordBuff.isEmpty()) {
			if (lineWidth >= maxWidth) {
				endBuffer.append('\n').append(indentBuff);
			}
			endBuffer.append(wordBuff);
		}

		return endBuffer.toString();
	}

	/**
	 * Adds <code>padCount</code> characters (specified with <code>padChar</code>) at the left of the string.
	 * If the string has multiple lines, the padding is added on all of them.
	 *
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
	 *
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

	public static String center(String str, int width) {
		return UtlString.center(str, width, '─');
	}

	public static String trim(String str, String pattern) {
		return str.replaceAll("^" + pattern + "+", "")
			.replaceAll(pattern + "+$", "");
	}

	public static String trim(String str) {
		return UtlString.trim(str, "[ \n\r\t]");
	}


	/**
	 * Remove all formatting colors or format from the string
	 */
	public static String removeSequences(String str) {
		return str.replaceAll("\033\\[[\\d;]*m", "");
	}
}
