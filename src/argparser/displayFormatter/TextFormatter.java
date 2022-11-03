package argparser.displayFormatter;

import java.util.ArrayList;
import java.util.Arrays;

public class TextFormatter {
	private ArrayList<FormatOption> formatOptions = new ArrayList<>();
	private Color foregroundColor;
	private Color backgroundColor;
	private String contents;

	public TextFormatter(String contents) {
		this.contents = contents;
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

	@Override
	public String toString() {
		// we'll just skip the whole thing if there's nothing to format or the contents are empty
		if (
			this.contents.length() == 0
				|| (this.formatOptions.size() == 0 && this.foregroundColor == null && this.backgroundColor == null)
		)
			return this.contents;

		StringBuilder str = new StringBuilder();

		if (foregroundColor != null) str.append(foregroundColor);
		if (backgroundColor != null) str.append(backgroundColor.toStringBackground());

		for (var fmt : formatOptions) str.append(fmt);

		str.append(contents);

		// reset
		for (var fmt : formatOptions) str.append(fmt.toStringReset());

		str.append(Color.BrightWhite);

		return str.toString();
	}

	public static TextFormatter ERROR(String msg) {
		return new TextFormatter(msg).setColor(Color.BrightRed).addFormat(FormatOption.Reverse, FormatOption.Bold);
	}
}