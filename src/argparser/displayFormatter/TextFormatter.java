package argparser.displayFormatter;

import argparser.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TextFormatter {
	public static boolean enableSequences = true;
	private final ArrayList<FormatOption> formatOptions = new ArrayList<>();
	private final List<TextFormatter> concatList = new ArrayList<>();
	private Color foregroundColor;
	private Color backgroundColor;
	private String contents;

	public TextFormatter(String contents) {
		this.contents = contents;
	}

	public TextFormatter() {
		this.contents = "";
	}

	public TextFormatter addFormat(FormatOption... options) {
		this.formatOptions.addAll(Arrays.asList(options));
		return this;
	}

	public TextFormatter setColor(Color foreground) {
		this.foregroundColor = foreground;
		return this;
	}

	public TextFormatter setColor(Color foreground, Color background) {
		this.foregroundColor = foreground;
		this.backgroundColor = background;
		return this;
	}

	public TextFormatter setBackgroundColor(Color background) {
		this.backgroundColor = background;
		return this;
	}

	public TextFormatter setContents(String contents) {
		this.contents = contents;
		return this;
	}

	public TextFormatter concat(TextFormatter... formatters) {
		this.concatList.addAll(Arrays.asList(formatters));
		return this;
	}

	@Override
	public String toString() {
		// we'll just skip the whole thing if there's nothing to format or the contents are empty
		if (
			(this.contents.length() == 0 || (this.formatOptions.size() == 0 && this.foregroundColor == null && this.backgroundColor == null) || !enableSequences)
				&& this.concatList.size() == 0 // we cant skip if we need to concat stuff!
		)
			return this.contents;

		final StringBuilder buffer = new StringBuilder();

		if (foregroundColor != null) buffer.append(foregroundColor);
		if (backgroundColor != null) buffer.append(backgroundColor.toStringBackground());

		for (var fmt : formatOptions)
			buffer.append(fmt);

		// add the contents
		buffer.append(contents).append(this.concatList.stream().map(TextFormatter::toString).reduce("", String::concat));

		// reset the formatting
		if (backgroundColor != null) {
			// this already resets everything, so we don't need to spend time on the rest
			buffer.append(FormatOption.RESET_ALL);
		} else {
			// reset each format option
			for (var fmt : formatOptions)
				buffer.append(fmt.toStringReset());

			// to reset the color we just set it back to white
			buffer.append(Color.BRIGHT_WHITE);
		}

		return buffer.toString();
	}

	public static TextFormatter ERROR(String msg) {
		return new TextFormatter(msg).setColor(Color.BRIGHT_RED).addFormat(FormatOption.REVERSE, FormatOption.BOLD);
	}

	/**
	 * Remove all formatting colors or format from the string
	 */
	public static String removeSequences(String str) {
		return str.replaceAll("\033\\[[\\d;]*m", "");
	}
}