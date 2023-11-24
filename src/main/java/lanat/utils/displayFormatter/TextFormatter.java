package lanat.utils.displayFormatter;

import lanat.utils.UtlString;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Allows to easily format text for display in a terminal.
 * <p>
 * Multiple formatters can be concatenated together. This is useful for when you want to
 * format a string that has multiple parts that need to be formatted differently.
 * </p>
 */
public class TextFormatter {
	/**
	 * When set to {@code false}, no formatting will be applied to text. Raw text will be generated without any
	 * color or formatting.
	 */
	public static boolean enableSequences = true;

	/**
	 * When set to {@code true}, the {@link #toString()} method will not add any terminal sequences, but rather
	 * return the sequences that would be added by marking them as {@code ESC[<sequence here>]}
	 */
	public static boolean debug = false;

	/** A list of all the formatting options that should be applied to the text. */
	private final @NotNull ArrayList<FormatOption> formatOptions = new ArrayList<>();

	/** A list of all the formatters that should be concatenated to this formatter. */
	private final @NotNull List<TextFormatter> concatList = new ArrayList<>();

	/** The parent formatter. Used when being concatenated to another formatter. */
	private @Nullable TextFormatter parent;
	private @Nullable Color foregroundColor;
	private @Nullable Color backgroundColor;
	private @NotNull String contents;
	private @Nullable String concatGap;

	/**
	 * Creates a new {@link TextFormatter} with the specified contents.
	 * @param contents The contents of the formatter.
	 */
	public TextFormatter(@NotNull String contents) {
		this.contents = contents;
	}

	/**
	 * Creates a new {@link TextFormatter} with the specified contents and foreground color.
	 * @param contents The contents of the formatter.
	 * @param foreground The foreground color of the formatter.
	 */
	public TextFormatter(@NotNull String contents, @Nullable Color foreground) {
		this(contents);
		this.foregroundColor = foreground;
	}

	/**
	 * Creates a new {@link TextFormatter} with no contents.
	 */
	public TextFormatter() {
		this.contents = "";
	}

	/**
	 * Adds the specified formatting options to the formatter.
	 * @param options The formatting options to add.
	 */
	public TextFormatter addFormat(@NotNull FormatOption... options) {
		this.formatOptions.addAll(Arrays.asList(options));
		return this;
	}

	/**
	 * Removes the specified formatting options from the formatter.
	 * @param options The formatting options to remove.
	 */
	public TextFormatter removeFormat(@NotNull FormatOption... options) {
		this.formatOptions.removeAll(Arrays.asList(options));
		return this;
	}

	/**
	 * Sets the foreground color of the formatter.
	 * @param foreground The foreground color of the formatter.
	 */
	public TextFormatter withForegroundColor(@Nullable Color foreground) {
		this.foregroundColor = foreground;
		return this;
	}

	/**
	 * Sets the background color of the formatter.
	 * @param background The background color of the formatter.
	 */
	public TextFormatter withBackgroundColor(@Nullable Color background) {
		this.backgroundColor = background;
		return this;
	}

	/**
	 * Sets the foreground and background color of the formatter.
	 * @param foreground The foreground color of the formatter.
	 * @param background The background color of the formatter.
	 */
	public TextFormatter withColors(@Nullable Color foreground, @Nullable Color background) {
		this.foregroundColor = foreground;
		this.backgroundColor = background;
		return this;
	}

	/**
	 * Sets the contents of the formatter.
	 * @param contents The contents of the formatter.
	 */
	public TextFormatter withContents(@NotNull String contents) {
		this.contents = contents;
		return this;
	}

	public TextFormatter withConcatGap(@Nullable String gap) {
		this.concatGap = gap;
		return this;
	}

	/**
	 * Concatenates the specified formatters to this formatter.
	 * @param formatters The formatters to concatenate.
	 */
	public TextFormatter concat(@NotNull TextFormatter... formatters) {
		for (TextFormatter formatter : formatters) {
			// if it was already added to another formatter, throw an exception
			if (formatter.parent != null) {
				throw new IllegalArgumentException("Cannot concatenate a formatter that is already concatenated to another formatter.");
			}
			formatter.parent = this;
			this.concatList.add(formatter);
		}
		return this;
	}

	/**
	 * Concatenates the specified strings to this formatter.
	 * @param strings The strings to concatenate.
	 */
	public TextFormatter concat(@NotNull String... strings) {
		for (var str : strings) {
			this.concatList.add(new TextFormatter(str));
		}
		return this;
	}

	/**
	 * Returns whether the formatter is simple. A formatter is simple if it has no formatting options, no foreground
	 * color, no background color, and no concatenated formatters.
	 * @return {@code true} if the formatter is simple
	 */
	public boolean isSimple() {
		return (this.contents.length() == 0 || this.formattingNotDefined())
			&& this.concatList.size() == 0; // we cant skip if we need to concat stuff!
	}

	/**
	 * Returns whether the formatter has no formatting options, no foreground color, and no background color.
	 * @return {@code true} if the formatter has no formatting options, no foreground color, and no background color
	 */
	public boolean formattingNotDefined() {
		return (
			this.foregroundColor == null
				&& this.backgroundColor == null
				&& this.formatOptions.isEmpty()
		);
	}

	/**
	 * Returns the start sequences to add to the contents of the formatter. This includes the foreground color, the
	 * background color, and all the formatting options.
	 * @return the start sequences to add to the contents of the formatter
	 */
	private @NotNull String getStartSequences() {
		if (this.formattingNotDefined()) return "";
		final var buffer = new StringBuilder();

		if (this.foregroundColor != null)
			buffer.append(this.foregroundColor);
		if (this.backgroundColor != null)
			buffer.append(this.backgroundColor.bg());

		for (var option : this.formatOptions) {
			buffer.append(option);
		}

		return buffer.toString();
	}

	private @NotNull String getEndSequences() {
		if (this.formattingNotDefined()) return "";
		final var buffer = new StringBuilder();

		if (this.backgroundColor != null) {
			var bgColor = this.getResetBgColor();

			// if bg color is null, we can just reset everything then.
			// also, it's not worth it to add any resetting sequences afterward, so just return this directly.
			if (bgColor == null)
				return FormatOption.RESET_ALL.seq();

			buffer.append(bgColor.bg());
		}

		for (var option : this.formatOptions) {
			buffer.append(option.reset());
		}

		if (this.foregroundColor != null) {
			buffer.append(this.getResetFgColor());
		}

		return buffer.toString();
	}

	/**
	 * Returns the {@link Color} that should properly reset the foreground color. This is determined by looking at the
	 * parent formatters. If no parent formatter has a foreground color, then {@link Color#BRIGHT_WHITE} is returned.
	 * @return the {@link Color} that should properly reset the foreground color
	 */
	private @NotNull Color getResetFgColor() {
		if (this.parent == null)
			return Color.BRIGHT_WHITE;

		if (this.parent.foregroundColor != null)
			return this.parent.foregroundColor;

		return this.parent.getResetFgColor();
	}

	/**
	 * Returns the {@link Color} that should properly reset the background color. This is determined by looking at the
	 * parent formatters. If no parent formatter has a background color, then {@code null} is returned.
	 * @return the {@link Color} that should properly reset the background color
	 */
	private @Nullable Color getResetBgColor() {
		if (this.parent == null)
			return null;

		if (this.parent.backgroundColor != null)
			return this.parent.backgroundColor;

		return this.parent.getResetBgColor();
	}

	/**
	 * Creates a new {@link String} with the contents and all the formatting applied.
	 */
	@Override
	public @NotNull String toString() {
		if (!TextFormatter.enableSequences || this.isSimple()) {
			return this.contents;
		}

		final var buff = new StringBuilder();

		if (this.contents.contains("\n")) {
			// for some reason, some terminals reset sequences when a new line is added.
			this.putContentsSanitized(buff);
		} else {
			buff.append(this.getStartSequences());
			buff.append(this.contents);
		}

		// then do the same thing for the concatenated formatters
		{
			@NotNull List<TextFormatter> concatList = this.concatList;

			for (int i = 0; i < concatList.size(); i++) {
				buff.append(concatList.get(i));

				if (this.concatGap != null && i < concatList.size() - 1)
					buff.append(this.concatGap);
			}
		}

		buff.append(this.getEndSequences());

		return buff.toString();
	}

	/**
	 * Adds the start sequences to the contents of the formatter. This is done by adding the start sequences after
	 * every new line. (and at the first line)
	 * @param buff The buffer to add the contents to.
	 */
	private void putContentsSanitized(@NotNull StringBuilder buff) {
		final var split = UtlString.splitAtLeadingWhitespace(this.contents);
		final var startSequences = this.getStartSequences();

		// start by adding the leading whitespace
		buff.append(split.first());

		// then add the start sequences
		buff.append(startSequences);

		char[] charArray = split.second().toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			var chr = charArray[i];

			// if we encounter a new line, and the next character is not a whitespace, then add the start sequences
			if (chr == '\n' && (i < charArray.length - 1 && !Character.isWhitespace(charArray[i + 1])))
				buff.append(startSequences);

			// add the character
			buff.append(chr);
		}
	}

	/** Returns a template for a {@link TextFormatter} that is used for errors */
	public static @NotNull TextFormatter ERROR(@NotNull String msg) {
		return new TextFormatter(msg, Color.BRIGHT_RED).addFormat(FormatOption.REVERSE, FormatOption.BOLD);
	}

	/**
	 * Returns a string with a terminal sequence with the specified code.
	 * (e.g. {@code "ESC[<code here>m"})
	 * <p>
	 * If {@link #debug} is set to {@code true}, then the text "ESC" will be used instead of the actual escape
	 * character.
	 * </p>
	 * @param code The code of the sequence.
	 * @return a string with a terminal sequence with the specified code
	 */
	static @NotNull String getSequence(int code) {
		if (TextFormatter.debug)
			return "ESC[" + code + "]";
		return "" + ESC + '[' + code + 'm';
	}

	/**
	 * Escape character which represents the start of a terminal sequence
	 */
	public static final char ESC = '\u001B';
}