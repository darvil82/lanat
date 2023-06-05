package lanat.utils;

import lanat.utils.displayFormatter.TextFormatter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public final class UtlString {
	private UtlString() {}

	/**
	 * Apply a predicate for each character in the string, if any fails, return <code>false</code>.
	 *
	 * @param str The string to check.
	 * @param fn The predicate to apply to each character.
	 */
	public static boolean matchCharacters(@NotNull String str, @NotNull Predicate<Character> fn) {
		for (char chr : str.toCharArray()) {
			if (!fn.test(chr)) return false;
		}
		return true;
	}

	/**
	 * Wrap a string in two strings at both sides. If the string is <code>null</code>, it will be replaced with
	 * <code>"null"</code>.
	 */
	public static @NotNull String surround(@Nullable String str, @NotNull String wrapper) {
		return str == null ? "null" : wrapper + str + wrapper;
	}

	/**
	 * Wrap a string in double quotes.
	 */
	public static @NotNull String surround(@Nullable String str) {
		return surround(str, "\"");
	}

	/**
	 * Get the longest line from the contents of a string. Lines are separated by newlines.
	 */
	public static @NotNull String getLongestLine(@NotNull String str) {
		return Arrays.stream(str.split("\n")).min((a, b) -> b.length() - a.length()).orElse("");
	}

	public static @NotNull String sanitizeName(@NotNull String name) {
		// remove all non-alphanumeric characters
		final var sanitized = UtlString.strip(name.replaceAll("[^a-zA-Z0-9 -]", ""), "[^a-zA-Z0-9]")
			.replaceAll(" ", "-");

		if (sanitized.isEmpty())
			throw new IllegalArgumentException("name must contain at least one alphanumeric character");

		return sanitized;
	}

	/**
	 * Wraps a string into multiple lines in order to fit in the given maximum width. Wrapping respects words and
	 * indentation, so no word will be split in two lines and indentation will be preserved.
	 *
	 * @param str The text to wrap.
	 * @param maxWidth The maximum width that the text should never exceed.
	 * @return The wrapped text.
	 */
	public static @NotNull String wrap(@NotNull String str, int maxWidth) {
		if (maxWidth <= 0)
			throw new IllegalArgumentException("maxWidth must be greater than 0");

		// contents are already short enough, no need to wrap.
		// also if we can't even split, why bother.
		if (str.length() <= maxWidth || !(str.contains(" ") || str.contains("\t")))
			return str;

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
	 * Adds <code>padCount</code> characters (specified with <code>padChar</code>) at the left of the string. If the
	 * string has multiple lines, the padding is added on all of them.
	 *
	 * @param str The string to pad.
	 * @param padCount The amount of characters to add.
	 * @param padChar The character to use for padding.
	 * @return The padded string.
	 */
	public static @NotNull String indent(@NotNull String str, int padCount, char padChar) {
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
	 * Adds <code>padCount</code> space characters at the left of the string. If the string has multiple lines, the
	 * padding is added on all of them.
	 *
	 * @param str The string to pad.
	 * @param padCount The amount of spaces to add.
	 * @return The padded string.
	 */
	public static @NotNull String indent(@NotNull String str, int padCount) {
		return UtlString.indent(str, padCount, ' ');
	}

	public static @NotNull String center(@NotNull String str, int width, char padChar) {
		final var paddingString = String.valueOf(padChar).repeat((width / 2) - (str.length() / 2) - 1);

		return paddingString + str + paddingString;
	}

	public static @NotNull String center(@NotNull String str, int width) {
		return UtlString.center(str, width, '─');
	}

	/**
	 * Remove all leading and trailing occurrences of the given regex from the string.
	 *
	 * @param str the string to strip
	 * @param regex the regex to remove
	 * @return the stripped string
	 */
	public static @NotNull String strip(@NotNull String str, @NotNull String regex) {
		return str.replaceAll("^" + regex + "+", "")
			.replaceAll(regex + "+$", "");
	}

	/**
	 * Remove all formatting colors or format from the string
	 */
	public static @NotNull String removeSequences(@NotNull String str) {
		return str.replaceAll(TextFormatter.ESC + "\\[[\\d;]*m", "");
	}

	/**
	 * Returns the count given appended to the string given. An <code>'s'</code> will be appended at the end if the
	 * count is not 1.
	 *
	 * @param str the string to append to
	 * @param count the count
	 * @return "count str" or "count strs" depending on the count
	 */
	public static @NotNull String plural(@NotNull String str, int count) {
		return count + " " + (count == 1 ? str : str + "s");
	}

	/**
	 * Returns the string given if it is not null, otherwise returns an empty string.
	 *
	 * @param str the string to check
	 * @return the string given or an empty string
	 */
	public static @NotNull String fromNullable(@Nullable String str) {
		return str == null ? "" : str;
	}

	/**
	 * Returns true if the string given is null or empty.
	 *
	 * @param str the string to check
	 * @return true if the string is null or empty
	 */
	public static boolean isNullOrEmpty(@Nullable String str) {
		return str == null || str.isEmpty();
	}

	/**
	 * Split a string by the given splitter. This is similar to {@link String#split(String)} but it will also ignore
	 * spaces around the splitter.
	 *
	 * @param str the string to split
	 * @param splitter the splitter
	 * @param max the maximum amount of splits
	 * @return the split string
	 */
	public static @NotNull String @NotNull [] split(@NotNull String str, @NotNull String splitter, int max) {
		return str.split(" *" + Pattern.quote(splitter) + " *", max);
	}

	/**
	 * {@link UtlString#split(String, String, int)} with max set to -1. (Default of {@link String#split(String)})
	 *
	 * @see UtlString#split(String, String, int)
	 */
	public static @NotNull String @NotNull [] split(@NotNull String str, @NotNull String splitter) {
		return UtlString.split(str, splitter, -1);
	}

	/**
	 * Split a string by the given splitter. This is similar to {@link String#split(String)} but it will also ignore
	 * spaces around the splitter.
	 *
	 * @param str the string to split
	 * @param splitter the splitter
	 * @return the split string
	 */
	public static @NotNull String @NotNull [] split(@NotNull String str, char splitter, int max) {
		return UtlString.split(str, String.valueOf(splitter), max);
	}

	/**
	 * {@link UtlString#split(String, char, int)} with max set to -1. (Default of {@link String#split(String)})
	 *
	 * @see UtlString#split(String, char, int)
	 */
	public static @NotNull String @NotNull [] split(@NotNull String str, char splitter) {
		return UtlString.split(str, String.valueOf(splitter), -1);
	}
}
