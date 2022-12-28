package argparser.utils.displayFormatter;

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

	public TextFormatter concat(String... strings) {
		this.concatList.addAll(Arrays.stream(strings).map(TextFormatter::new).toList());
		return this;
	}

	public boolean isSimple() {
		return (
			this.contents.length() == 0
			|| (
				this.formatOptions.size() == 0
				&& this.foregroundColor == null
				&& this.backgroundColor == null
			)
			|| !enableSequences
		) && this.concatList.size() == 0; // we cant skip if we need to concat stuff!
	}

	@Override
	public String toString() {
		// we'll just skip the whole thing if there's nothing to format or the contents are empty
		if (this.isSimple())
			return this.contents;

		final StringBuilder buffer = new StringBuilder();

		Runnable pushFormat = () -> {
			if (foregroundColor != null) buffer.append(foregroundColor);
			if (backgroundColor != null) buffer.append(backgroundColor.toStringBackground());
			for (var fmt : formatOptions)
				buffer.append(fmt);
		};

		// push the format
		pushFormat.run();

		// add the contents
		buffer.append(this.contents);

		// concat the other formatters
		for (int i = 0; i < this.concatList.size(); i++) {
			final var formatter = this.concatList.get(i);
			buffer.append(formatter);
			// add our format back after each concat. Not the last one though, since we will be resetting it anyway
			if (i < this.concatList.size() - 1 && !formatter.isSimple())
				pushFormat.run();
		}

		// reset the formatting
		if (backgroundColor != null) {
			// this already resets everything, so we don't need to spend time on the rest
			buffer.append(FormatOption.RESET_ALL);
		} else {
			// reset each format option
			if (!this.formatOptions.isEmpty())
				for (var fmt : this.formatOptions)
					buffer.append(fmt.toStringReset());

			// to reset the color we just set it back to white
			if (this.foregroundColor != null)
				buffer.append(Color.BRIGHT_WHITE);
		}

		return buffer.toString();
	}

	public static TextFormatter ERROR(String msg) {
		return new TextFormatter(msg).setColor(Color.BRIGHT_RED).addFormat(FormatOption.REVERSE, FormatOption.BOLD);
	}
}