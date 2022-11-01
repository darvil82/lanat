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
}