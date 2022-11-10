package argparser.displayFormatter;

import java.util.ArrayList;
import java.util.Arrays;

public class TextFormatter {
	private ArrayList<FormatOption> formatOptions = new ArrayList<>();
	private Color foregroundColor;
	private Color backgroundColor;
	private String contents;
	public static boolean DEBUG_DISABLE = false;

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
		this.backgroundColor = null;
		return this;
	}

	public TextFormatter setColor(Color foreground, Color background) {
		this.foregroundColor = foreground;
		this.backgroundColor = background;
		return this;
	}

	public TextFormatter setContents(String contents) {
		this.contents = contents;
		return this;
	}

	@Override
	public String toString() {
		// we'll just skip the whole thing if there's nothing to format or the contents are empty
		if (
			this.contents.length() == 0
				|| (this.formatOptions.size() == 0 && this.foregroundColor == null && this.backgroundColor == null)
				|| DEBUG_DISABLE
		)
			return this.contents;

		StringBuilder str = new StringBuilder();

		if (foregroundColor != null) str.append(foregroundColor);
		if (backgroundColor != null) str.append(backgroundColor.toStringBackground());

		for (var fmt : formatOptions) str.append(fmt);

		str.append(contents);

		// reset
		for (var fmt : formatOptions) str.append(fmt.toStringReset());

		str.append(Color.BRIGHT_WHITE);

		return str.toString();
	}

	public static TextFormatter ERROR(String msg) {
		return new TextFormatter(msg).setColor(Color.BRIGHT_RED).addFormat(FormatOption.REVERSE, FormatOption.BOLD);
	}

	/**
	 * Remove all formatting colors or format from the string
	 */
	public static String removeSequences(String str) {
		return str.replaceAll("\033\\[[0-9;]*m", "");
	}
}